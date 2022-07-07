"""로그 기능"""

import logging
import logging.handlers


class Log:
    @staticmethod
    def create_logger(logger_name:str) -> object:
        """로그 생성 함수"""
        logger = logging.getLogger(logger_name)
    
        if len(logger.handlers) > 0:
            return logger

        logger.setLevel(logging.DEBUG)

        formatter = logging.Formatter('\n[%(levelname)s|%(name)s|%(filename)s:%(lineno)s]%(asctime)s>%(message)s')

        stream_handler = logging.StreamHandler()
        stream_handler.setLevel(logging.DEBUG)
        stream_handler.setFormatter(formatter)

        logger.addHandler(stream_handler)

        file_handler = logging.FileHandler('/home/PromotionPawn/Project/SilverCare/Server/test_server/logs/log.log')
        file_handler.setFormatter(formatter)

        logger.addHandler(file_handler)

        return logger
    
    @staticmethod
    def record(logger_name:str, description:str) -> None:
        logger = Log.create_logger(logger_name)
        logger.info(description)