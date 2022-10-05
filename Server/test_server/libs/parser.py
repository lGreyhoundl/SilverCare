""" 파싱 클래스 """
import json
import hashlib
from urllib import parse

from libs.inform import UserSignUpInform
from libs.inform import UserLoginInform
from libs.inform import UserLocationInform
from libs.inform import UserDeviceInform

from libs.encryption import Encryption


class Parser():

    @staticmethod
    def signup_parser(request:json) -> object:
        """ 회원가입 정보 데이터 파싱 """
        user_signup_inform = UserSignUpInform

        try:
            request_json = json.dumps(request, ensure_ascii=False)
            request_diction = json.loads(request_json)
            
        except Exception:
            return user_signup_inform

        try:
            sha256_pwd = Encryption.sha256_hashing(request_diction['user_pwd'])
            
            encode_user_residence = request_diction['user_residence']
            decode_user_residence = parse.unquote(encode_user_residence)
            user_residence = decode_user_residence.replace("+", " ")
            user_residence = user_residence.replace("\n", "")
            
            user_signup_inform = UserSignUpInform(
                user_id = request_diction['user_id'],
                user_pwd = sha256_pwd,
                user_contact_protector = request_diction['user_contact_protector'],
                user_contact_elder = request_diction['user_contact_elder'],
                user_residence = user_residence,
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
    
    @staticmethod
    def device_data_parser(request:json) -> object:
        """ 장치 데이터 파싱"""
        user_device_inform = UserDeviceInform
        
        try:
            request_json = json.dumps(request)
            request_diction = json.loads(request_json)
            encode_device_name = request_diction['device_name']
            decode_device_name = parse.unquote(encode_device_name)
            
        except Exception:
            return user_device_inform
        
        
        try:
            user_device_inform = UserDeviceInform(
                    user_id = request_diction['user_id'],
                    device_name = decode_device_name,
                    device_status = request_diction['device_status'],
                    err_state = True)
        except Exception:
            return user_device_inform
        
        return user_device_inform

            
        
        