package marketpredictions.test

import java.sql.Timestamp;

object CalendarUtil
{

  def timestamp(year:Int, month: Int, dayOfMonth: Int, hh: Int = 0, mm: Int = 0):Timestamp =
  {
    val c = java.util.Calendar.getInstance();
    c.set(year,month+1,dayOfMonth, hh, mm);
    new Timestamp(c.getTime.getTime);
  }

  def addDays(x:Timestamp, n: Int): Timestamp =
  {
    new Timestamp(x.getTime() + n*24*60*60*1000L)
  }

}

// vim: set ts=4 sw=4 et:
