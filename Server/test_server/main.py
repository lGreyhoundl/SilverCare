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

@jit
@app.route("/echo",  methods = ['POST'])
def echo():
    """회원가입 기능"""
    Log.record("/echo", get_json)
    get_json = request.get_json()
    return json.dumps(get_json, ensure_ascii=False), 200


@jit
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
    
@jit
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
    else:
        response_login["error"] = "501"
        
    cursor.close()
    conn.close()
    return json.dumps(response_login, ensure_ascii=False), 200
    # return json.dumps(200, ensure_ascii=False), 200
    
    
@jit
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
    
@jit
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

    

if __name__ == "__main__":
    # ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS)
    # ssl_context.load_cert_chain(certfile='certificate/cert.pem', keyfile='certificate/key.pem', password='EMSW')
    # app.run(host='0.0.0.0', port=3000, ssl_context=ssl_context)
    app.run(host='0.0.0.0', port=3000)
    
    # app.run()

