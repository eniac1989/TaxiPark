package taxipark

import kotlin.math.roundToInt

/*
 * Task #1. Find all the drivers who performed no trips.
 */
fun TaxiPark.findFakeDrivers(): Set<Driver> {
    if (this.trips.size == 0)
        return this.allDrivers
    val distinctDriver = this.trips.distinctBy { it.driver }.map { it.driver }.toSet()
    return allDrivers.filter { it !in distinctDriver }.toSet()
}

/*
 * Task #2. Find all the clients who completed at least the given number of trips.
 */
fun TaxiPark.findFaithfulPassengers(minTrips: Int): Set<Passenger> {
    if (this.trips.size == 0)
        return emptySet()
    if (minTrips == 0)
        return allPassengers
    val groupBy = this.trips.flatMap { it.passengers }.groupingBy { it }.eachCount().filter { it.value >= minTrips }.keys
    return allPassengers.filter { (it in groupBy) }.toSet()
}


/*
 * Task #3. Find all the passengers, who were taken by a given driver more than once.
 */
fun TaxiPark.findFrequentPassengers(driver: Driver): Set<Passenger> {
    var myMAp = mutableMapOf<Passenger, Int>()

    this.trips.forEach {
        for (i in 0 until it.passengers.size) {
            if (it.driver.equals(driver)) {
                val driverCount = myMAp.get(it.passengers.elementAt(i))
                var plus = 0
                if (driverCount != null)
                    plus = driverCount?.plus(1)
                else
                    plus = 1
                myMAp.put(it.passengers.elementAt(i), plus)
            }
        }
    }
    val passengers = myMAp.filter { it.value > 1 }
    val finalPassenger = passengers.map { it.key }.toSet()
    return finalPassenger
}


/*
 * Task #4. Find the passengers who had a discount for majority of their trips.
 */
fun TaxiPark.findSmartPassengers(): Set<Passenger> {
    var result = hashMapOf<Passenger, Int>()
    var retResult = mutableSetOf<Passenger>()
    this.trips.forEach {
        for (i in 0..it.passengers.size - 1) {
            if (!result.containsKey(it.passengers.elementAt(i))) {
                if (it.discount != 0.0 && it.discount != null)
                    result.put(it.passengers.elementAt(i), 1)

            } else {
                if (it.discount != 0.0 && it.discount != null) {
                    val discountCount = result.get(it.passengers.elementAt(i))?.plus(1)
                    if (discountCount != null) {
                        result.put(it.passengers.elementAt(i), discountCount)
                    }
                }
            }
        }
    }
    val flatMap = this.trips.flatMap { it.passengers }.groupingBy { it }.eachCount().toMap()
    result.forEach {
        if (result.get(it.key) != null) {
            val discontC = result.get(it.key)
            if (discontC != null && flatMap[it.key] != null)
                if ((flatMap[it.key]!!.minus(discontC))!! < discontC)
                    retResult.add(it.key)
        }
    }
    return retResult
}

/*
 * Task #5. Find the most frequent trip duration among minute periods 0..9, 10..19, 20..29, and so on.
 * Return any period if many are the most frequent, return `null` if there're no trips.
 */
fun TaxiPark.findTheMostFrequentTripDurationPeriod(): IntRange? {

    val groupBy = this.trips.groupBy {
        val start = it.duration / 10 * 10
        val end = start + 9
        start..end
    }.maxBy { it.value.size }

    return groupBy?.key
}

/*
 * Task #6.
 * Check whether 20% of the drivers contribute 80% of the income.
 */
fun TaxiPark.checkParetoPrinciple(): Boolean {
    var myResult = mutableMapOf<Driver, Double>()
    val driverCounts = this.allDrivers.size
    val tPercentDriver = driverCounts * 0.2
    if(trips.isEmpty())
        return false
    val groupBy = this.trips.groupBy { it.driver }.forEach {
        var sum = 0.0
        for (i in 0 until it.value.size) {
            sum += (1 - (it.value.get(i).discount ?: 0.0)) * (it.value.get(i).duration + it.value.get(i).distance)
            myResult[it.key] = sum
        }
    }
    val totalCostPercent = this.trips.sumByDouble {
        (1 - (it.discount ?: 0.0)) * (it.duration + it.distance)
    } * 0.8
    var sumPercent = 0.0
    val toMap = myResult.toList().sortedByDescending { (_, value) -> value }.toMap()


    var i = 0
    toMap.entries.forEach {
        if (i <= tPercentDriver.roundToInt()-1) {
            sumPercent += it.value
            i++
        } else
            return@forEach
    }
    return sumPercent >= totalCostPercent
}