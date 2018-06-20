void ReadGPSData(object s, EventArgs e){ 
	string buff = string.Empty;
	buff = sp.ReadExisting();
 
        string[] gpsEx = buff.Split('$');
	for (int i = 0; i < gpsEx.Length; i++)
        {
		if (gpsEx[i].Split(',').Length > 10)
                { 
                    //GPRMC를 가져온다. 
                    if (gpsEx[i].Split(',')[0] == "GPRMC")
                    { 
                        string validation = gpsEx[i].Split(',')[2];
                        if (validation == "V")
                        { 
                        }
                        if (validation == "A")
                        {
                            //Time 
                            string time = gpsEx[i].Split(',')[1];
                            string date = gpsEx[i].Split(',')[9];

                            string dd = date.Substring(0, 2);
                            string mm = date.Substring(2, 2);
                            string yy = date.Substring(4, 2);

                            string hh = time.Substring(0, 2);
                            string mn = time.Substring(2, 2);
                            string ss = time.Substring(4, 2);

                            int hour = int.Parse(hh);
                            hour = hour + 9;
                            int year = int.Parse(yy) + 2000;
                            int month = int.Parse(mm);
                            int day = int.Parse(dd);
                            int min = int.Parse(mn);
                            int sec = int.Parse(ss);

                            nowDT = new DateTime(year, month, day, hour, min, sec);
 
                            //Location : Latitude, Longitude
                            string latitude = gpsEx[i].Split(',')[3];
                            string longitude = gpsEx[i].Split(',')[5];

                            string[] las = latitude.Split('.');
                            string[] los = longitude.Split('.');
 
                            string temp = las[0];
                            char[] ch = temp.ToCharArray();
 
                            string la_do = string.Empty;
                            string la_bun = string.Empty;

                            int cutCount = 0;
                            if (ch.Length == 4)
                            {
                                cutCount = 2;
                            }
                            else if (ch.Length == 5)
                            {
                                cutCount = 3;
                            }
                            for (int a = 0; a < ch.Length; a++)
                            {
                                if (a < cutCount)
                                {
                                    la_do += ch[a].ToString();
                                }
                                else
                                {
                                    la_bun += ch[a].ToString();
                                }
                            }
                            la_bun += "." + las[1];
 
                            string temps = los[0];
                            char[] chs = temps.ToCharArray();
                            string lo_do = string.Empty;
                            string lo_bun = string.Empty;
 
                            if (chs.Length == 4)
                            {
                                cutCount = 2;
                            }
                            else if (chs.Length == 5)
                            {
                                cutCount = 3;
                            }
                            for (int a = 0; a < chs.Length; a++)
                            {
                                if (a < cutCount)
                                {
                                    lo_do += chs[a].ToString();
                                }
                                else
                                {
                                    lo_bun += chs[a].ToString();
                                }

                            }
                            lo_bun += "." + los[1];
 
                            double d_la = double.Parse(la_do) + (double.Parse(la_bun) / 60);
                            double d_lo = double.Parse(lo_do) + (double.Parse(lo_bun) / 60);
                            GPS_latitude = d_la.ToString();
                            GPS_logitude = d_lo.ToString();

			}
			}
		} 
	}  
}
