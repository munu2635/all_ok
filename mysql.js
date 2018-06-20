var mysql = require('mysql');
var connection = mysql.createConnection({
  host:'127.0.0.1', port:3306, user:'root', password:'123456', database : "powersupply"
})

connection.connect(function(err){
    if (err){
        console.log('mysql connection is fail ');
        console.log(err);
        throw err;
    } else {
        console.log('mysql connection is success');
    }
});

var decrease = function (id) {
	var fest_sup_supply_num 
	var slow_sup_supply_num 
	
	connection.query('SELECT fest_sup from supply WHERE id=?', [id] , function (error, results, fields) {
		if (error) console.log(error);
		else fest_sup_supply_num = results;
	}); 
	connection.query('SELECT slow_sup from supply WHERE id=?', [id], function (error, results, fields) {
  		if (error) console.log(error);
  		else slow_sup_supply_num = results;
	}); 
  	
	if(fest_sup_supply_num > 0){
    		connection.query('UPDATE supply SET fest_sup=? WHERE id =?' [ fest_sup_supply_num-1 , id], function (error, results, fields) {
  			if (error) console.log(err);
	 		else console.log(rows);
		}); 
    		return 1; 
  	} else if(slow_sup_supply_num > 0) { 
    		connection.query('UPDATE supply SET slow_sup=? WHERE id=?', [slow_sup_supply_num-1 , id], function (error, results, fields) {
  			if (error) console.log(err);
	 		else console.log(rows);
		}); 
   	 	return 2;
  	}
  	return -1;
};


var increase = function(id, supply) {
	var fest_sup_supply_num 
	var slow_sup_supply_num 
	
	connection.query('SELECT fest_sup from supply WHERE id=?', [id] , function (error, results, fields) {
		if (error) console.log(error);
		else fest_sup_supply_num = results;
	}); 
	connection.query('SELECT slow_sup from supply WHERE id=?', [id], function (error, results, fields) {
  		if (error) console.log(error);
  		else slow_sup_supply_num = results;
	}); 
  	

  	if(supply == 1){
   		 connection.query('UPDATE supply SET fest_sup=? WHERE id =?' [ fest_sup_supply_num+1 , id], function (error, results, fields) {
  			if (error) console.log(err);
	 		else console.log(rows);
		}); 
  	} else if(supply == 2) { 
    		query('UPDATE supply SET slow_sup=? WHERE id=?', [slow_sup_supply_num+1 , id], function (error, results, fields) {
  			if (error) console.log(err);
	 		else console.log(rows);
		}); 
  	}
};

connection.end();
