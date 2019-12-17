# -*- coding: UTF-8 -*-
"""
Project Seele
@author : Rinka
@date   : 2019/12/17
"""
from seele.clazz import ResourcingContext


class ResourceServiceImpl:
    """
    Resource Service.
    RS is responsible for control workitems life-cycle, and provide work queue operations for participants.
    DO NOT use this internal class directly, use `RS` instead.
    """

    @staticmethod
    def perform_submit(ctx: ResourcingContext) -> None:
        """
        Perform resource service on a workitem which is submitted to RS.
        :param ctx: a context contains workitem to be resourcing
        """
        pass

    @staticmethod
    def perform_complete(ctx: ResourcingContext) -> None:
        """
        Handle completed workitem RS action, the last life-cycle for this context.
        :param ctx: a context contains workitem to be resourcing
        """
        pass
