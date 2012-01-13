This is a java implementation of a item-based recommender algorithm. It uses the Java API tools for parallelization.

Benchmark use:
for i in {1..8}; do echo "using "$i" cores on the 342k dataset"; java -Xms6g -Xmx6g ch/epfl/sweng/recommender/tests/tester $i ../../dataset_mod5_342k.dat; done;