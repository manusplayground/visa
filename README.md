# VISA Staff Interview Question

Spring boot, JPA application used for making 2 REST endpoints. One for listing latest available appointment date times and another for booking an available appointment. 

Appointments can be booked for multiple tiers of a service (tier1 or Tier 2 or Tier 3).

Multiple agents can be serving appointments for the organization

Users can book available appointment slots under certain constraints like:
  - only one slot can be booked at a time
  - slot shoud not be beyond next 60 days
  - slot should not be on a sunday and should only be between 8 am - 5 pm
  - only returning users can book a slot between 8 am - 10 am if the slot being booked is beyond 3 days in the future.


# Complete Question
https://github.com/manusplayground/visa/blob/master/Screen%20Shot%202019-02-05%20at%2011.02.00%20AM.png

# Documentation
please refer - https://github.com/manusplayground/visa/blob/master/src/main/resources/ReadMe.pdf
