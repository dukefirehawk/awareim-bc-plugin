# AIM custom process for BigCommerce #
================================================

This is a private repository for custom process to integrate with BigCommerce

#Features Implemented#
- Sync product
- Sync customer
- Sync order and order item

##System##
- JDK 1.7
- jackson-core 2.2.0
- jackson-annotations 2.2.0
- jackson-databind 2.2.0

#TODO#

##Bugs##
- Out of memory at Source

##Important##
- Each run is capped at 6000 records

#Development Progress#

##[24/11/2015]##
- Completed send order to BC
- Completed sync inventory from BC

##[20/11/2015]##
- Fixed bug in sending order to BC

##[16/11/2015]##
- Add Sync Coupon and Category

##[23/10/2015]##
- Added place order for sending order to BC

##[22/10/2015]##
- Added placing coupon and order

##[17/10/2015]##
- Added relationship between order and order item
- Added ability to overwrite corrupted record

##[11/10/2015]##
- Simplified property file loading

##[07/10/2015]##
- Added get_input_parameters query in BSV to provide backend input to BcProductProcess, BcCustomerProcess and BcOrderProcess.

##[24/09/2015]##
- Releases TG2

