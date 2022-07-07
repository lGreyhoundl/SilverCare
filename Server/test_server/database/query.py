from dataclasses import dataclass

@dataclass(frozen=True)
class Query:
    signup:str = 'INSERT INTO ems_app.users(UserID, UserPWD, UserEmail, UserContactProtector, UserContactElder) VALUES (%s, %s,%s, %s, %s);'
    id_check:str = 'SELECT UserID FROM ems_app.users WHERE UserID = %s;'
    login:str = 'SELECT UserPWD FROM ems_app.users WHERE UserID = %s;'