package com.picfun

import java.util.*

/**
 * @author Secret
 * @since 2019/10/30
 */
class Ants {

    fun timeSolution(distanceList:IntArray, length:Int){

        Arrays.sort(distanceList)

        var minTime = 0
        var maxTime = 0

        distanceList.forEach {
            val leftTime = it
            val rightTime = length - it
            if(leftTime > rightTime){
                if(rightTime > minTime){
                    minTime = rightTime
                }
                if(leftTime > maxTime){
                    maxTime = leftTime
                }
            }else{
                if(leftTime > minTime){
                    minTime = leftTime
                }
                if(rightTime > maxTime){
                    maxTime = rightTime
                }
            }
        }

        println("the minTime and maxTime of the Ants cross wooden pole is $minTime and $maxTime")

    }

    companion object{

        @JvmStatic
        fun main(args: Array<String>) {
            val ants = Ants()
            val antsDistanceArray = intArrayOf(3,7,11,17,23)
            ants.timeSolution(antsDistanceArray,27)
        }

    }

}