cmake_minimum_required(VERSION 3.20)
project(p4_handout C)

set(CMAKE_C_STANDARD 99)

add_executable(p4_handout textproc.c logging.c parse.c util.c)
