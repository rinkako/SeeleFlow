# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/17
"""
from seele.workflow import WorkitemStateType


class Workitem:
    """
    Workitem is an abstract for a task instance need to be handle.
    """

    def __init__(self):

        # workitem global unique id
        self.wid: str = None

        # create time
        self.create_time: str = None

        # update time
        self.update_time: str = None

        # argument dict
        self.arguments: dict = None

        # TODO: a vector contains histories that recording this workitem all transitions
        self.history = None

        # state of workitem
        self.state: WorkitemStateType = None

        # TODO: exception container
        self.exceptions = None

        # TODO: result container
        self.result = None

