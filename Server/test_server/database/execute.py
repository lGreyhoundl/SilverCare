import json
from typing import Any
import pandas as pd
from libs.log import Log 
from database.query import Query

class DataBaseTask():
    
    def __init__(self, conn, cursor):
        self.conn = conn
        self.cursor = cursor
        
        
    def signup(self, user_signup_inform:object) -> bool:
        try:
            self.cursor.execute(Query.signup, (user_signup_inform.user_id,
                                                    user_signup_inform.user_pwd,
                                                    user_signup_inform.user_contact_protector,
                                                    user_signup_inform.user_contact_elder,
                                                    user_signup_inform.user_residence
                                                    ))
            self.conn.commit()
            Log.record("signup", f"{user_signup_inform.user_id} - {self.cursor._last_executed}")
            return True
        
        except Exception as err:
            Log.record("signup", f"{user_signup_inform.user_id} - {err}")
            return False
        
    
    def id_check(self, user_signup_inform:object) -> bool:
        
        self.cursor.execute(Query.id_check, (user_signup_inform.user_id))        
        
        try:
            list(self.cursor)[0]
            return True
        
        except Exception :
            return False
        
    
    def login(self, user_login_inform:object) -> str:
        
        self.cursor.execute(Query.login, (user_login_inform.user_id))
        
        try:
            Log.record("login", f"{user_login_inform.user_id} - {self.cursor._last_executed}")
            pwd = str(list(self.cursor)[0])
            pwd = pwd[2:-3]
            
            return pwd
            
        except Exception :
            return "False"


    def get_contact(self, user_login_inform:object) -> dict:
        self.cursor.execute(Query.get_contact, (user_login_inform.user_id))
        
        try:
            Log.record("get_contect", f"{user_login_inform.user_id} - {self.cursor._last_executed}")
            contact = str(list(self.cursor)[0])
            contact = contact[1:-1].replace("'","")
            contact = contact.split(",")
            
            return contact
            
        except Exception :
            return "False"
        
    def get_home_address(self, user_login_inform:object) -> dict:
        self.cursor.execute(Query.get_home_address, (user_login_inform.user_id))
        
        try:
            Log.record('get_home_address', f"{user_login_inform.user_id} - {self.cursor._last_executed}")
            address = list(self.cursor)[0]  
            return address[0]
        
        except Exception:
            return "False"