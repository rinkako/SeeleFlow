# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/17
"""
from seele.workitem import Workitem


class ResourcingContext:
    """
    Resourcing Context maintains all resource service principals that guide RS
    to handle the workitem.
    """
    def __init__(self):
        # resourcing unique id
        self.rsid: str = None

        # process runtime unique id
        self.rtid: str = None

        # priority, bigger schedule faster
        self.priority: int = 0

        # execution cost in ms
        self.execution_time_span: int = 0

        # TODO: service type, indicate the action RS should do
        self.rs_type = None

        # TODO: resourcing context
        self.rs_properties: dict = None

        # binding workitem
        self.workitem: Workitem = None
