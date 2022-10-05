""" firebase 관리 코드 """

import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

from database.define import DataBaseDefine

class FireBase():
    """ firebase realtime database 관리 클래스 """

    @staticmethod
    def firebase_conn():
        """ firebase connection 연결 함수 """
        cred = credentials.Certificate('certificate/silvercare-355312-firebase-adminsdk-w9bjt-ac32a76caf.json')
        firebase_admin.initialize_app(cred, {
        'databaseURL': DataBaseDefine.firebase
        })

    @staticmethod
    def firebase_insert_id(user_signup_inform:object) -> int:
        """ 회원가입시 최초 아이디 insert 함수 """
        try:
            FireBase.firebase_conn()
        except Exception:
            pass
        ref = db.reference('/')
        users_ref = ref.child('users')
        users_ref.update({
            f'{user_signup_inform.user_id}':
            {   
                "latitude" :  "None",
                "longitude" : "None"
            }
        })
        

    @staticmethod
    def update_location(user_location_data:object) -> None:
        """ 위치 업데이트 """
        try:
            FireBase.firebase_conn()
        except Exception:
            pass
        
        ref = db.reference('users')
        users_ref = ref.child(f'{user_location_data.user_id}')
        users_ref.update({
                    "latitude": f"{user_location_data.latitude}",
                    "longitude": f"{user_location_data.longitude}"
                })


    @staticmethod
    def get_location(user_location_data:object) -> None:
        """ 위치 가져오기 """
        try:
            FireBase.firebase_conn()
        except Exception:
            pass
    
    @staticmethod 
    def set_device_name(user_device_data:object) -> None:
        """ 장치 등록 """
        try:
            FireBase.firebase_conn()
        except Exception:
            pass
    
        ref = db.reference(f'/users/{user_device_data.user_id}/')
        device_ref = ref.child(f'Device')
        
        new_device_ref = device_ref.push() 
        new_device_ref.set({
                    'room' : f'{user_device_data.device_name}', 
                    'status': f'{user_device_data.device_status}'        
                })
        
        
    @staticmethod 
    def set_device_status(user_device_data:object) -> None:
        """ 장치 등록 """
        try:
            FireBase.firebase_conn()
        except Exception:
            pass
    
        ref = db.reference(f'/users/{user_device_data.user_id}/')
        device_ref = ref.child(f'Device').get()
        
        for key in device_ref.keys():
            if device_ref[key]['room'] == user_device_data.device_name:
                ref = db.reference(f'/users/{user_device_data.user_id}/Device')
                ref = ref.child(f'{key}')
                ref.update({
                    "room": f"{user_device_data.device_name}",
                    "status": f"{user_device_data.device_status}"
                })
            