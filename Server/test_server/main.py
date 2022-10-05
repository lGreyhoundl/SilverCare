"""flask route main code"""
import json
import ssl
from flask import Flask
from flask import request
from numba import jit

from libs.log import Log 
from libs.parser import Parser 
from database.dbconn import DataBaseConnection
from database.execute import DataBaseTask
from database.firebase import FireBase
app = Flask(__name__)


# pm2 start main.py  --interpreter python3.9 --name "FlaskServer"
# pm2 start main.py  --interpreter python3.9 --name "FlaskServer" --watch --ignore-watch="/home/ec2-user/* /home/ec2-user/logs/*"



@app.route("/echo",  methods = ['POST'])
def echo():
    """ echo"""
    get_json = request.get_json()
    print(get_json)
    Log.record("/echo", get_json)
    return json.dumps("200", ensure_ascii=False), 200



@app.route("/signup",  methods = ['POST'])
def signup():
    """회원가입 기능"""
    get_json = request.get_json()
    user_signup_inform:object = Parser.signup_parser(get_json)
    
    response_singup = {}
    if user_signup_inform.err_state == False:
        response_singup["error"] = "400"
    
    conn = DataBaseConnection.connection()
    cursor = conn.cursor()
    
    db_task = DataBaseTask(conn, cursor)
    
    id_check = db_task.id_check(user_signup_inform)
    
    if id_check == False :
        db_task.signup(user_signup_inform)
        FireBase.firebase_insert_id(user_signup_inform)
        response_singup["error"] = "200"
    else : 
        response_singup["error"] = "401"

    cursor.close()
    conn.close()
    
    
    Log.record("response_singup",response_singup)
    return json.dumps(response_singup, ensure_ascii=False), 200
    # return json.dumps(200, ensure_ascii=False), 200
    

@app.route("/login", methods = ['POST'])
def login():
    """ 장비 등록 기능 """
    get_json = request.get_json()
    user_inform = Parser.login_parser(get_json)
    
    response_login = {}
    if user_inform.err_state == False:
        response_login["error"] = "500"
    
    conn = DataBaseConnection.connection()
    cursor = conn.cursor()
    
    db_task = DataBaseTask(conn, cursor)
    
    pwd = db_task.login(user_inform)
    
    if user_inform.user_pwd == pwd :  
        response_login["error"] = "200"
        response_login = db_task.get_contact(user_inform)
        get_home_address = db_task.get_home_address(user_inform)
        
        response_login = {"error": "200",
                          "protector_contact": response_login[0],
                          "elder_contact": response_login[1],
                          "address": get_home_address}
    else:
        response_login["error"] = "501"
        
    cursor.close()
    conn.close()
    return json.dumps(response_login, ensure_ascii=False), 200
    # return json.dumps(200, ensure_ascii=False), 200
    
    
@app.route("/login/location",  methods = ['POST'])
def update_location():
    """위치 업데이트 기능"""
    get_json = request.get_json()
    user_location_inform:object = Parser.location_parser(get_json)
    
    response_location = {}
    if user_location_inform.err_state == False:
        response_location["error"] = "600"
    
    else:
        FireBase.update_location(user_location_inform)
        response_location["error"] = "200"
    
    Log.record("response_location",response_location)
    
    return json.dumps(response_location, ensure_ascii=False), 200
    

@app.route("/login/RequestLocation",  methods = ['POST'])
def send_location():
    """위치 전송 기능"""
    get_json = request.get_json()
    user_location_inform:object = Parser.location_parser(get_json)
    
    response_location = {}
    if user_location_inform.err_state == False:
        response_location["error"] = "600"
    
    else:
        FireBase.update_location(user_location_inform)
        response_location["error"] = "200"
    
    Log.record("response_location",response_location)
    
    return json.dumps(response_location, ensure_ascii=False), 200



@app.route("/login/DeviceRegister",  methods = ['POST'])
def device_register():
    """ 장치 등록 기능"""
    get_json = request.get_json()
    user_device_inform:object = Parser.device_data_parser(get_json)
    
    response_device = {}
    if user_device_inform.err_state == False:
        response_device["error"] = "600"
    
    else:
        FireBase.set_device_name(user_device_inform)
        response_device["error"] = "200"
    
    Log.record("response_device",response_device)
    
    return json.dumps(response_device, ensure_ascii=False), 200


@app.route("/login/DeviceStatus",  methods = ['POST'])
def device_status():
    """ 장비 상태 업데이트 """
    get_json = request.get_json()
    user_device_inform:object = Parser.device_data_parser(get_json)
    
    response_device = {}
    if user_device_inform.err_state == False:
        response_device["error"] = "600"
    
    else:
        FireBase.set_device_status(user_device_inform)
        response_device["error"] = "200"
    
    Log.record("response_device",response_device)
    
    return json.dumps(response_device, ensure_ascii=False), 200


if __name__ == "__main__":
    # ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS)
    # ssl_context.load_cert_chain(certfile='certificate/cert.pem', keyfile='certificate/key.pem', password='EMSW')
    # app.run(host='0.0.0.0', port=3000, ssl_context=ssl_context)
    app.run(host='0.0.0.0', port=3000)
    
    # app.run()

