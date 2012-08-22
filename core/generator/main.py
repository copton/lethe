#!/usr/bin/python

import sys
import application

if __name__ == '__main__':
    application = application.Application()
    status = application.main(sys.argv)
    sys.exit(status)

