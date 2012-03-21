package marketpredictions.test

object CalendarUtil
{

  def timestamp(year:Int, month: Int, dayOfMonth: Int, hh: Int = 0, mm: Int = 0):java.sql.Timestamp =
  {
    val c = java.util.Calendar.getInstance();
    c.set(year,month+1,dayOfMonth, hh, mm);
    new java.sql.Timestamp(c.getTime.getTime);
  }

}

// vim: set ts=4 sw=4 et:
