# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/16
"""

from enum import Enum


class WorkitemStateType(Enum):
    """
    Enum：Workitem resourcing status.
    """

    """
    A workitem is created, never apply any resourcing service.
    """
    Created = 0

    """
    A workitem is cancelled before it was fired to resource service.
    """
    Cancelled = 1

    """
    A workitem is allocated to a resource, but have not been accepted by the resource yet.
    """
    Allocated = 2

    """
    A workitem is fired to resource service, while resource service cannot find any resource to handle it.
    """
    BadAllocated = 3

    """
    A workitem is accepted by a resource, but the resource has not process it yet. 
    """
    Accepted = 4

    """
    A workitem is executing by a resource.
    """
    Running = 5

    """
    A workitem completed at a resource but some exceptions occurred when executing.
    """
    Exception = 6

    """
    A workitem completed at a resource.
    """
    Completed = 7

    """
    A workitem is set to be completed at a resource by human when it was still in running state. 
    """
    ForcedCompleted = 8

    """
    A workitem is waiting for any resource to pull it and accept it.
    """
    Offered = 9

    """
    A workitem is paused when it executing at a resource, this action is not forcible and only give a hint to resource.
    """
    Suspended = 10

    """
    A workitem is returned from a Allocated state and wait for next transition by human.
    """
    UnOffered = 11


class WorkQueueType(Enum):
    """
    Enum: Work queue type. A workitem must be in one of the listed queue without `WORKLISTED`.
          Resource Service will move workitem cross these queues.
    """

    """
    A queue for workitems never resourcing.
    """
    UNDEFINED = 0

    """
    A queue for workitems waiting for pulling and accept.
    """
    OFFERED = 1

    """
    A queue contains all allocated workitems.
    """
    ALLOCATED = 2

    """
    A queue contains all running workitems.
    """
    STARTED = 3

    """
    
    A queue contains all suspended workitems. 
    """
    SUSPENDED = 4

    """
    A queue contains all workitems that were returned from allocated state and wait for human re-allocation.
    """
    UNOFFERED = 5

    """
    A special queue which contains all workitems in all queues except `UNDEFINED`.
    """
    WORKLISTED = 6
