==================================
Anthony Dubis (ajd2194)
Aikaterini Iliakopoulou (ai2315)
COMS W4112 Project #2
DUE April 22 2015
==================================
[001] List of Files
[002] How to Run
[003] High-Level Description

==================================
[001] LIST OF FILES
==================================
BPOptimizer.java
Record.java
config.txt
query.txt
output.txt
stage2.sh
Makefile
==================================
[002] HOW TO RUN
==================================
To compile go to the directory where the folders are located and run "make".
To run the algorithm, run "./stage2.sh" query.txt config.txt
==================================
[003] HIGH LEVEL DESCRIPTION
==================================
The main class of the program is BPOptimizer, which reads the input files, runs the algorithm to find
the optimal plan for each set of selectivities and outputs the plan to standard output. The Record class is used
for storing all necessary information for a particular subset plan, including
the terms the plan utilizes, their selectivities, plan's cost, the left and right branch 
and the value of bit b that shows whether the plan is a no-branch plan or not. 

In BPOptimizer, the input files, query.txt and config.txt are read in that order inside main to get the list of selectivities and the 
processor's parameters the algorithm is going to use to evaluate the costs of the different plans respectively. As soon as the parameters
are set inside the program, the algorithm runs for each row of selectivities to produce the optimal plan for that particular set of functions.
This is done by calling the method findOptimalPlan(...) of BPOptimizer, which in summary creates all the subsets of records in an increasing order, runs the first 
part of the dynamic algorithm that calculates the costs for the logical ANDs and for the no branching plans, then runs the second part of the 
algorithm that evaluates the costs of the branching plans and finally outputs the optimal plan by reading the left and right branches of the 
separate subset plans in a recursive manner. 

The subsets are created and read with the help of bitmaps. Specifically, if we have for example a list of 4 functions then we define a binary number
[0 0 0 0], where each bit represents the absence or presence of the function in the respective subset plan. We use 1 for presence and 0 for 
absence. We then manipulate those bitmaps to create all unique subset combinations in an increasing order. This is done inside createSubsetsOfTerms(...).

The first part of the algorithm runs inside considerLogicalAndNoBranchingPlans(...) method, where all the costs of the subset plans are computed 
using example 4.5. Then if the cost of the respective no-branch plan, given by the cost function in example 4.4 is smaller than the previous cost,
the latter is replaced by the no-branch cost and the bit for using a no-branch subset plan is set to true. 
The second part of the algorithm is executed inside considerBranchingAndPlans(...) method for all the possible combinations of two subset plans in view of
finding those plans that can be combined in an optimal way. Specifically, if two subset plans don't overlap and the c- and d-metrics conditions are met,
the two plans are combined and the optimal cost if calculated based on FCost function given by definition 4.7 and formula (1) on the same page in  
paper [1]. By the end of the execution, the left-most record of the optimal plan is located in the last row of array A, where we store all subset plans in 
increasing order. 

The left-most record plan (the one that points to the optimal plan computed by the algorithm), is passed on method outputPlan(...), which is responsible for 
printing the output plan to the standard output (console). Ultimately, the optimal plan is found by traversing all the right children of the record
plans contained in A, starting from the initial left-most record. If the no - branch plan is selected for a particular record, then the output is
adjusted respectively. Finally, the output of our algorithm for the sample set of selectivities contained in the query.txt file, can be found in output.txt file.
Please not that we copied the output from the console to the output.txt and that the program doesn't write to the latter file, but instead prints the output to
the standard output, as requested by the instructors.


***** Sources *****
[1]  K. A. Ross. Selection conditions in main memory. TODS, 29:132Ð161, 2004.

[2] To learn how to read variables from .properties files, the following link
was referenced:
http://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/

[3] To learn how to create Java Makefiles, the following link was reference: 
http://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html

[4] In order to create the subsets of the terms, I referenced this post on StackOverflow: http://stackoverflow.com/questions/7206442/printing-all-possible-subsets-of-a-list




