""" 파싱 클래스 """
import json
import hashlib
from libs.inform import UserSignUpInform
from libs.inform import UserLoginInform
from libs.inform import UserLocationInform
from libs.encryption import Encryption

class Parser():

    @staticmethod
    def signup_parser(request:json) -> object:
        """ 회원가입 정보 데이터 파싱 """
        user_signup_inform = UserSignUpInform

        try:
            request_json = json.dumps(request)
            request_diction = json.loads(request_json)

        except Exception:
            return user_signup_inform

        try:
            sha256_pwd = Encryption.sha256_hashing(request_diction['user_pwd'])
    
            user_signup_inform = UserSignUpInform(
                user_id = request_diction['user_id'],
                user_pwd = sha256_pwd,
                user_contact_protector = request_diction['user_contact_protector'],
                user_contact_elder = request_diction['user_contact_elder'],
                user_email = request_diction['user_email'],
                err_state = True)

        except Exception:
            return user_signup_inform

        return user_signup_inform


    @staticmethod
    def login_parser(request:json) -> object:
        """ 로그인 정보 데이터 파싱"""

        user_login_inform = UserLoginInform

        try:
            request_json = json.dumps(request)
            request_diction = json.loads(request_json)

        except Exception:
            return user_login_inform
            
        
        try:
            sha256_pwd = Encryption.sha256_hashing(request_diction['user_pwd'])

            user_login_inform = UserLoginInform(
                user_id = request_diction['user_id'],
                user_pwd = sha256_pwd,
                err_state = True)
                        
        except Exception:
            return user_login_inform
        
        return user_login_inform


    @staticmethod
    def location_parser(request:json) -> object:
        """ 위치 정보 데이터 파싱"""
        user_location_inform = UserLocationInform
        
        try:
            request_json = json.dumps(request)
            request_diction = json.loads(request_json)

        except Exception:
            return user_location_inform
        
        try:
            user_location_inform = UserLocationInform(
                    user_id = request_diction['user_id'],
                    latitude = request_diction['user_latitude'],
                    longitude = request_diction['user_longitude'],
                    err_state = True)

        except Exception:
            return user_location_inform

        return user_location_inform
    