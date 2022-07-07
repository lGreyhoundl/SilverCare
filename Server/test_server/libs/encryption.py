""" 암호화 기능 """
import json
import hashlib

class Encryption():
    """암호화 기능"""

    @staticmethod
    def sha256_hashing(user_pwd:str) -> str:
        """ 사용자 패스워드 sha256 암호화"""
        return hashlib.sha256(user_pwd.encode('utf-8')).hexdigest()