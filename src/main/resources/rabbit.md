#坑点
1. RabbitMQ不允许使用不同的参数设定重新定义已经存在的队列，
并且会向尝试如此做的程序返回一个错误，channel error 错误406