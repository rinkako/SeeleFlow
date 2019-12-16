# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/16
"""

from enum import Enum


class WorkitemStateType(Enum):
    """
    枚举：工作项资源状态
    """

    """
    工作项创建成功，尚未资源分配
    """
    Created = 0

    """
    工作项在还没执行之前就被取消了
    """
    Cancelled = 1

    """
    工作项已经指派给某个资源，但资源还未接受
    """
    Allocated = 2

    """
    工作项已经创建成功，但没有合适的资源可以执行
    """
    BadAllocated = 3

    """
    工作项已经被某个资源接受，但还未开始处理
    """
    Accepted = 4

    """
    工作项正在被执行
    """
    Running = 5

    """
    工作项由于异常而终止
    """
    Exception = 6

    """
    工作项在资源上自然执行完毕
    """
    Completed = 7

    """
    工作项在资源上执行的时候被人为地设置为已经完成
    """
    ForcedCompleted = 8

    """
    工作项处于待被主动拉取状态
    """
    Offered = 9

    """
    工作项执行中被设置为挂起状态
    """
    Suspended = 10

    """
    工作项处于未分配状态，这个状态是由Allocated状态退回后到达的状态
    """
    UnOffered = 11


class WorkQueueType(Enum):
    """
    枚举：工作队列类型
    """

    """
    尚未资源服务的队列
    """
    UNDEFINED = 0

    """
    已经处于待拉取状态的队列
    """
    OFFERED = 1

    """
    已经指派的队列
    """
    ALLOCATED = 2

    """
    已经启动的队列
    """
    STARTED = 3

    """
    挂起的队列
    """
    SUSPENDED = 4

    """
    从Offered队列退回工作项的队列
    """
    UNOFFERED = 5

    """
    除了UNDEFINED队列之外的全部工作队列的透视队列（不存在实际队列）
    """
    WORKLISTED = 6
