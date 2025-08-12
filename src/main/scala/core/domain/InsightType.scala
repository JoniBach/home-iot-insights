package core.domain

import core.entities.Reading

sealed trait InsightType {
  def extractValue(reading: Reading): Double
}

object InsightType {
  case object Temperature extends InsightType {
    def extractValue(reading: Reading): Double = reading.temperature
  }
  
  case object Humidity extends InsightType {
    def extractValue(reading: Reading): Double = reading.humidity
  }
  
  case object Pressure extends InsightType {
    def extractValue(reading: Reading): Double = reading.pressure
  }
}
