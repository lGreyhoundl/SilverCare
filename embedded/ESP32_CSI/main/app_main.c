/* Wi-Fi CSI console Example

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/

#include <errno.h>
#include <string.h>
#include <stdio.h>

#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"
#include "esp_event.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "esp_err.h"
#include "esp_console.h"

#include "esp_wifi.h"
#include "lwip/inet.h"
#include "lwip/netdb.h"
#include "lwip/sockets.h"

#include "driver/gpio.h"
#include "driver/rmt.h"
#include "hal/uart_ll.h"
#include "led_strip.h"
#include "sdkconfig.h"

#include "esp_radar.h"
#include "csi_commands.h"



#ifdef CONFIG_IDF_TARGET_ESP32C3
#define CONFIG_LED_STRIP_GPIO        GPIO_NUM_8
#elif CONFIG_IDF_TARGET_ESP32S3
#define CONFIG_LED_STRIP_GPIO        GPIO_NUM_48
#else
#define CONFIG_LED_STRIP_GPIO        GPIO_NUM_12
#endif

#define CONFIG_LESS_INTERFERENCE_CHANNEL    11

#define GPIO GPIO_NUM_2


static uint8_t s_led_state = 0;

static float g_corr_threshold = 1.10;
static uint8_t g_action_value = 0;
static uint8_t g_action_id    = 0;

static led_strip_t *g_strip_handle = NULL;
static xQueueHandle g_csi_info_queue = NULL;



static const char *TAG  = "app_main";

char recv_buf[64];

#define WEB_SERVER "tera.dscloud.me"
#define WEB_PORT "3000"
#define WEB_PATH "/login/DeviceStatus"

// 거실 : %EA%B1%B0%EC%8B%A4
// 주방 : %EC%A3%BC%EB%B0%A9
// 안방 : %EC%95%88%EB%B0%A9
// 화장실 : %ED%99%94%EC%9E%A5%EC%8B%A4

static const char *REQUEST_FALSE = "POST " WEB_PATH " HTTP/1.1\r\n"
    "Host: "WEB_SERVER":"WEB_PORT"\r\n"
    "User-Agent: esp-idf/1.0 esp32\r\n"
    "Content-Type: application/json\r\n"
    "Content-Length: 100\r\n"
    "\r\n"
    "{\r\n    \"user_id\": \"test\",\r\n    \"device_name\": \"%EA%B1%B0%EC%8B%A4\",\r\n    \"device_status\": \"False\"\r\n}";


static const char *REQUEST_TRUE = "POST " WEB_PATH " HTTP/1.1\r\n"
    "Host: "WEB_SERVER":"WEB_PORT"\r\n"
    "User-Agent: esp-idf/1.0 esp32\r\n"
    "Content-Type: application/json\r\n"
    "Content-Length: 99\r\n"
    "\r\n"
    "{\r\n    \"user_id\": \"test\",\r\n    \"device_name\": \"%EA%B1%B0%EC%8B%A4\",\r\n    \"device_status\": \"True\"\r\n}";

int s, r;

char flag = 0;

char true_counter = 0;
char false_counter = 0;

char move_flag = 0;

static void configure_led(void)
{
    ESP_LOGI(TAG, "configured to blink GPIO LED");
    gpio_reset_pin(GPIO);
    /* Set the GPIO as a push/pull output */
    gpio_set_direction(GPIO, GPIO_MODE_OUTPUT);
}

static void blink_led(uint8_t s_led_state)
{
    gpio_set_level(GPIO, s_led_state);
}

static void http_init(void* pvParameters)
{
    const struct addrinfo hints = {
        .ai_family = AF_INET,
        .ai_socktype = SOCK_STREAM,
    };
    struct addrinfo *res;
    struct in_addr *addr;
    
    int err = getaddrinfo(WEB_SERVER, WEB_PORT, &hints, &res);
    addr = &((struct sockaddr_in *)res->ai_addr)->sin_addr;
    s = socket(res->ai_family, res->ai_socktype, 0);

    if(connect(s, res->ai_addr, res->ai_addrlen) != 0) {
            ESP_LOGE(TAG, "... socket connect failed errno=%d", errno);
            close(s);
            freeaddrinfo(res);
            vTaskDelay(4000 / portTICK_PERIOD_MS);
        }

        ESP_LOGI(TAG, "... connected");
        freeaddrinfo(res);
}

static void wifi_init(void)
{
    ESP_LOGI(TAG, "---------------wifi_init()---------------");



    esp_err_t ret = nvs_flash_init();

    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }

    ESP_ERROR_CHECK(ret);

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();

    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    ESP_ERROR_CHECK(esp_wifi_set_storage(WIFI_STORAGE_RAM));
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_NULL));
    ESP_ERROR_CHECK(esp_wifi_start());
    ESP_ERROR_CHECK(esp_wifi_set_ps(WIFI_PS_NONE));
    ESP_ERROR_CHECK(esp_wifi_set_channel(CONFIG_LESS_INTERFERENCE_CHANNEL, WIFI_SECOND_CHAN_BELOW));
}

static struct {
    struct arg_str *corr_threshold;
    struct arg_int *action;
    struct arg_int *mode;
    struct arg_int *id;
    struct arg_end *end;
} radar_args;

static int wifi_cmd_radar(int argc, char **argv)
{
    if (arg_parse(argc, argv, (void **) &radar_args) != ESP_OK) {
        arg_print_errors(stderr, radar_args.end, argv[0]);
        return ESP_FAIL;
    }

    if (radar_args.corr_threshold->count) {
        float threshold = atof(radar_args.corr_threshold->sval[0]);

        if (threshold >= 1.0 && threshold <= 2.0) {
            g_corr_threshold = threshold;
        } else {
            ESP_LOGE(TAG, "If the setting fails, the absolute threshold range of std is: 0.0 ~ 2.0");
        }
    }

    if (radar_args.action->count) {
        g_action_value = radar_args.action->ival[0];
    }

    if (radar_args.id->count) {
        g_action_id = radar_args.id->ival[0];
    }

    if (radar_args.mode->count && radar_args.mode->ival[0]) {
        if (g_action_value) {
            esp_radar_action_calibrate_start(RADAR_ACTION_STATIC);
        } else {
            esp_radar_action_calibrate_stop(RADAR_ACTION_STATIC);
        }
    }

    ESP_LOGI(TAG, "value: %d, id: action %d, threshold: %.2f",
             g_action_value, g_action_id, g_corr_threshold);

    return ESP_OK;
}

void cmd_register_radar(void)
{
    radar_args.corr_threshold = arg_str0("t", "corr_threshold", "<1.0 ~ 2.0>", "Set the corr threshold of std");
    radar_args.mode           = arg_int0("m", "mode", "0 ~ 1 (label: 0, calibrate: 1)", "Mode selection");
    radar_args.action         = arg_int0("a", "actions", "<0 ~ 20>", "CSI data with actions");
    radar_args.id             = arg_int0("i", "id", "id", "CSI data with actions id");
    radar_args.end = arg_end(4);

    const esp_console_cmd_t radar_cmd = {
        .command = "radar",
        .help = "Radar config",
        .hint = NULL,
        .func = &wifi_cmd_radar,
        .argtable = &radar_args
    };

    ESP_ERROR_CHECK(esp_console_cmd_register(&radar_cmd));
}

static void csi_data_print_task(void *arg)
{
    ESP_LOGI(TAG, "---------------csi_data_print_task()---------------");
    
    wifi_csi_filtered_info_t *info = NULL;
    static char buffer[8196];
    
    // int i = 0;
    while (xQueueReceive(g_csi_info_queue, &info, portMAX_DELAY)) {
        wifi_pkt_rx_ctrl_t *rx_ctrl = &info->rx_ctrl;
        static uint32_t s_count = 0;
        size_t len = 0;
        if (!s_count) {
            ESP_LOGI(TAG, "================ CSI RECV ================");
            len += sprintf(buffer + len, "type,id,action_id,action,mac,rssi,rate,sig_mode,mcs,bandwidth,smoothing,not_sounding,aggregation,stbc,fec_coding,sgi,noise_floor,ampdu_cnt,channel,secondary_channel,local_timestamp,ant,sig_len,rx_state,len,first_word,data\n");
            esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
            esp_err_t ret = esp_console_run("ping --interval 10 --count 0", &repl_config);
            {
                ESP_LOGI(TAG, "radar --mode 1 --action 3");
                esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
                esp_err_t ret = esp_console_run("radar --mode 1 --action 3", &repl_config);
            }
        }

        // if (i > 30){
        //     ESP_LOGI(TAG, "radar --mode 1 --action 0");
        //     esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
        //     esp_err_t ret = esp_console_run("radar --mode 1 --action 0", &repl_config);   
        // }
        
        len += sprintf(buffer + len, "CSI_DATA,%d,%d,%d," MACSTR ",%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%u,%d,%d,%d",
                       s_count++, g_action_id, g_action_value,  MAC2STR(info->mac), rx_ctrl->rssi, rx_ctrl->rate, rx_ctrl->sig_mode,
                       rx_ctrl->mcs, rx_ctrl->cwb, rx_ctrl->smoothing, rx_ctrl->not_sounding,
                       rx_ctrl->aggregation, rx_ctrl->stbc, rx_ctrl->fec_coding, rx_ctrl->sgi,
                       rx_ctrl->noise_floor, rx_ctrl->ampdu_cnt, rx_ctrl->channel, rx_ctrl->secondary_channel,
                       rx_ctrl->timestamp, rx_ctrl->ant, rx_ctrl->sig_len, rx_ctrl->rx_state);

        len += sprintf(buffer + len, ",%d,%d,\"[%d", info->valid_len, 0, info->valid_data[0]);

        for (int i = 1; i < info->valid_len; i++) {
            len += sprintf(buffer + len, ",%d", info->valid_data[i]);
        }

        len += sprintf(buffer + len, "]\"\n");
        printf("%s", buffer);
        free(info);


        int timer = (xTaskGetTickCount() * (1000 / configTICK_RATE_HZ)) / 1000 - 5;
        // ESP_LOGI(TAG, "%d", timer);

        if(timer < 30 && flag == 0)
        {
            s_led_state = !s_led_state;
            blink_led(s_led_state);
        }    
        if (timer > 30 && flag == 0)
        {
            ESP_LOGI(TAG, "radar --mode 1 --action 0");
            esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
            esp_err_t ret = esp_console_run("radar --mode 1 --action 0", &repl_config);
            flag = 1;
            s_led_state = 1;
            blink_led(s_led_state);
        }
        // i++;
    }
    vTaskDelete(NULL);
}

void wifi_csi_raw_cb(const wifi_csi_filtered_info_t *info, void *ctx)
{
    wifi_csi_filtered_info_t *q_data = malloc(sizeof(wifi_csi_filtered_info_t) + info->valid_len);
    *q_data = *info;
    memcpy(q_data->valid_data, info->valid_data, info->valid_len);

    if (!g_csi_info_queue || xQueueSend(g_csi_info_queue, &q_data, 0) == pdFALSE) {
        free(q_data);
    }
}

static void wifi_radar_cb(const wifi_radar_info_t *info, void *ctx)
{
    static uint32_t s_count = 0;
    static uint32_t s_trigger_move_corr_count = 0;
    static uint32_t s_last_move_time = 0;
    static float s_corr_last = 0;
    bool trigger_move_corr_flag = 0;

    float corr_diff = (info->amplitude_corr_max - info->amplitude_corr_min) * g_corr_threshold;

    if (corr_diff < 0){
        corr_diff = 1;
    }

    if (fabs(info->amplitude_corr - s_corr_last) > corr_diff) {
        s_trigger_move_corr_count++;
    } else {
        s_trigger_move_corr_count = 0;
    }

    if (s_trigger_move_corr_count > 0) {
        trigger_move_corr_flag = true;
        g_strip_handle->set_pixel(g_strip_handle, 0, 0, 0, 255);
        g_strip_handle->refresh(g_strip_handle, 100);

        s_last_move_time = xTaskGetTickCount() * (1000 / configTICK_RATE_HZ);
        ESP_LOGW(TAG, "Someone moved");
        ESP_LOGI(TAG, "True Counter++ %d", true_counter);
        true_counter++;      

        if (true_counter > 3 && move_flag == 0)
        {   
            ESP_LOGI(TAG,"True_Counter : %d", true_counter);
            http_init("http_post_task");
            if (write(s, REQUEST_TRUE, strlen(REQUEST_TRUE)) < 0) {
                ESP_LOGE(TAG, "... socket send failed");
                close(s);
                vTaskDelay(4000 / portTICK_PERIOD_MS);
            }
        
            ESP_LOGI(TAG, "... socket send success");
            bzero(recv_buf, sizeof(recv_buf));
            r = read(s, recv_buf, sizeof(recv_buf)-1);
            for(int i = 0; i < r; i++) {
                putchar(recv_buf[i]);
            }
           close(s);
           true_counter = 0;
           false_counter = 0;
           move_flag = 1;
        }
    }
    else
    {
        false_counter++;
        ESP_LOGI(TAG, "Fales Counter++ %d", false_counter);
        if(false_counter > 40 && move_flag == 1)
        {
            ESP_LOGI(TAG,"False_Counter : %d", false_counter);
            http_init("http_post_task");
            if (write(s, REQUEST_FALSE, strlen(REQUEST_FALSE)) < 0) {
                ESP_LOGE(TAG, "... socket send failed");
                close(s);
                vTaskDelay(4000 / portTICK_PERIOD_MS);
            }
        
            ESP_LOGI(TAG, "... socket send success");
            bzero(recv_buf, sizeof(recv_buf));
            r = read(s, recv_buf, sizeof(recv_buf)-1);
            for(int i = 0; i < r; i++) {
                putchar(recv_buf[i]);
            }
            close(s);

            false_counter = 0;
            true_counter = 0;
            move_flag = 0;
        }   
    }

    if (s_last_move_time && xTaskGetTickCount() * (1000 / configTICK_RATE_HZ) - s_last_move_time > 3 * 1000) {
        s_last_move_time  = 0;
        g_strip_handle->clear(g_strip_handle, 100);
    }

    // ESP_LOGW(TAG, "s_count: %d, time_spent: %d, std: %f, corr: %f, corr_estimate: %f, min_std: %f, avg_std: %f",
    //          s_count, info->time_spent, info->amplitude_std, info->amplitude_corr, info->amplitude_corr_estimate, amplitude_std_min, amplitude_std_avg);

    if (!s_count) {
        ESP_LOGI(TAG, "================ RADAR RECV ================");
        printf("type,id,corr,corr_max,corr_min,corr_threshold,corr_trigger\n");
    }

    printf("RADAR_DADA,%d,%f,%f,%f,%f,%d\n", 
            s_count++, info->amplitude_corr, s_corr_last + corr_diff, s_corr_last - corr_diff, 
            g_corr_threshold, trigger_move_corr_flag);

    s_corr_last = info->amplitude_corr;
}

static bool g_get_ip_flag = false;

/* Event handler for catching system events */
static void wifi_event_handler(void *arg, esp_event_base_t event_base,
                               int32_t event_id, void *event_data)
{
    ESP_LOGI(TAG, "-------------wifi_event_handler----------------");
    ESP_LOGI(TAG, "event_base: %s, event_id: %d", event_base, event_id);
    // ESP_LOGI(TAG, "arg: %s, event_data: %s", *arg, *event_data);
    
    if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        g_get_ip_flag = true;
        wifi_radar_config_t radar_config = {0};
        wifi_ap_record_t ap_info;

    
        esp_wifi_sta_get_ap_info(&ap_info); 
        esp_wifi_radar_get_config(&radar_config);
        memcpy(radar_config.filter_mac, ap_info.bssid, sizeof(ap_info.bssid));
        esp_wifi_radar_set_config(&radar_config);
    }

    esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
    ESP_LOGI(TAG, "---------------PING START---------------");
    esp_err_t ret = esp_console_run("ping --interval 10 --count 0", &repl_config);
    ESP_ERROR_CHECK(ret);
    // ESP_LOGI(TAG, "---------------PIMG STOP---------------");
    // ret = esp_console_run("ping --abort", &repl_config);
}




void app_main(void)
{
    
    configure_led();
    blink_led(1);
    

    /**
     * @brief Install ws2812 driver
     */
    rmt_config_t config = RMT_DEFAULT_CONFIG_TX(CONFIG_LED_STRIP_GPIO, RMT_CHANNEL_0);
    // set counter clock to 40MHz
    config.clk_div = 2;
    ESP_ERROR_CHECK(rmt_config(&config));
    ESP_ERROR_CHECK(rmt_driver_install(config.channel, 0, 0));
    led_strip_config_t strip_config = LED_STRIP_DEFAULT_CONFIG(1, (led_strip_dev_t)config.channel);
    g_strip_handle = led_strip_new_rmt_ws2812(&strip_config);
    g_strip_handle->set_pixel(g_strip_handle, 0, 255, 255, 255);
    ESP_ERROR_CHECK(g_strip_handle->refresh(g_strip_handle, 100));

    /**
     * @brief Initialize Wi-Fi radar
     */
    wifi_init();
    esp_wifi_radar_init();

    /**
     * @brief Register serial command
     */
    esp_console_repl_t *repl = NULL;
    esp_console_repl_config_t repl_config = ESP_CONSOLE_REPL_CONFIG_DEFAULT();
    esp_console_dev_uart_config_t uart_config = ESP_CONSOLE_DEV_UART_CONFIG_DEFAULT();
    repl_config.prompt = "csi>";


    ESP_LOGI(TAG, "---------------repl_config---------------");
    blink_led(0);
    ESP_LOGI(TAG, "%s", repl_config.prompt);

    ESP_ERROR_CHECK(esp_console_new_repl_uart(&uart_config, &repl_config, &repl));

#if CONFIG_IDF_TARGET_ESP32 || CONFIG_IDF_TARGET_ESP32S2
    /**< Fix serial port garbled code due to high baud rate */
    uart_ll_set_sclk(UART_LL_GET_HW(CONFIG_ESP_CONSOLE_UART_NUM), UART_SCLK_APB);
    uart_ll_set_baudrate(UART_LL_GET_HW(CONFIG_ESP_CONSOLE_UART_NUM), CONFIG_ESP_CONSOLE_UART_BAUDRATE);
#endif

    cmd_register_system();
    ESP_LOGI(TAG, "---------------cmd_register_system---------------");
    blink_led(1);
    cmd_register_ping();
    ESP_LOGI(TAG, "---------------cmd_register_ping---------------");
    blink_led(0);
    cmd_register_wifi_config();
    ESP_LOGI(TAG, "---------------cmd_register_wifi_config---------------");
    blink_led(1);
    cmd_register_wifi_scan();
    ESP_LOGI(TAG, "---------------cmd_register_wifi_scan---------------");
    blink_led(0);
    cmd_register_radar();
    ESP_LOGI(TAG, "---------------cmd_register_radar---------------");
    blink_led(1);
    
        
    // ESP_ERROR_CHECK(esp_console_start_repl(repl));
    ESP_LOGI(TAG, "---------------esp_console_start_repl---------------");
    blink_led(0);
    
    g_csi_info_queue = xQueueCreate(10, sizeof(void *));


    ESP_ERROR_CHECK(esp_event_handler_register(IP_EVENT, IP_EVENT_STA_GOT_IP, &wifi_event_handler, NULL));

    

    wifi_radar_config_t radar_config = {
        .wifi_radar_cb        = wifi_radar_cb,
        .wifi_csi_filtered_cb = wifi_csi_raw_cb,
        .filter_mac           = {0x1a, 0x00, 0x00, 0x00, 0x00, 0x00},
    };


    esp_wifi_radar_set_config(&radar_config);
    ESP_LOGI(TAG, "---------------esp_wifi_radar_set_config---------------");
    blink_led(0);

    esp_wifi_radar_start();
    ESP_LOGI(TAG, "---------------esp_wifi_radar_start---------------");
    blink_led(1);

    xTaskCreate(csi_data_print_task, "csi_data_print", 4 * 1024, NULL, 0, NULL);    

    
    esp_err_t ret = esp_console_run("wifi_config --ssid SECURE --password Roottoor", &repl_config);
    ESP_ERROR_CHECK(ret);
    blink_led(0);

    // vTaskDelay(100);
    // ret = esp_console_run("ping --interval 10 --count 0", &repl_config);
    

}
