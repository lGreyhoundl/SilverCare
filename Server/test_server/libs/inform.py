""" 데이터 구조체"""
from asyncio import FastChildWatcher
from dataclasses import dataclass

@dataclass
class UserSignUpInform():
    """회원가입 유저 구조체"""
    user_id:str = "None"
    user_pwd:str = "None"
    user_contact_protector:str = "None"
    user_contact_elder:str = "None"
    user_residence:str = "None"
    err_state:bool = False


@dataclass
class UserLoginInform():
    """유저 로그인 구조체"""
    user_id:str = "None"
    user_pwd:str = "None"
    err_state:bool = False


@dataclass
class UserLocationInform():
    """유저 위치 구조체"""    
    user_id:str = "None"
    latitude:str = "None"
    longitude:str = "None"
    err_state:bool = False
    
    
@dataclass
class UserDeviceInform():
    """ 유저 장치 구조체 """
    user_id:str = "None"
    device_name:str = "None"
    device_status:str = "None"
    err_state:bool = False
    