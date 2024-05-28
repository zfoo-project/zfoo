cmake_minimum_required(VERSION 3.23)
project(cpp)

set(CMAKE_CXX_STANDARD 17)

set(PROJECT_MAIN cpp)

# main
file(GLOB_RECURSE MAIN_SOURCES zfoocpp/*.c**)
file(GLOB_RECURSE MAIN_HEADERS zfoocpp/*.h**)

# test
file(GLOB_RECURSE TEST_SOURCES test/*.c**)
file(GLOB_RECURSE TEST_HEADERS test/*.h**)

add_executable(${PROJECT_MAIN}
        main.cpp
        ${TEST_SOURCES} ${TEST_HEADERS}
        ${MAIN_SOURCES} ${MAIN_HEADERS})

target_include_directories(${PROJECT_NAME} SYSTEM PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})