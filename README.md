pickAndRoll
===========

pickAndRoll is a webapp for visualizing NCAA Men's basketball statistics. It's anticipated use is in the creation of March Madness brackets. 

Compare servlet – Displays basic statistics (conference wins/losses, overall wins/losses, points per game, points allowed per game) of two teams in a table for easy comparison. Servlet is responsible for getting data from DB and formatting it in a table. 

Visualize servlet – Uses d3.js to draw a scatterplot conveying team's overall percentage and progress in March Madness tournament (1st round, 2nd round, ... , tournament champion) year by year. Servlet returns JSON array to be handled by d3.js function. 

getAndStore – Program which uses the jsoup API to parse HTML of pages containing statistics and consolidate data into a DB table. 