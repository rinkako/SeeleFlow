# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/17
"""


class Workitem:
    """
    Workitem is an abstract for a task instance need to be handle.
    """

    def __init__(self):

        # workitem global unique id
        self.wid: str = None

