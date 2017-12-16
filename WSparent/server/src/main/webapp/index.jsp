<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<!DOCTYPE html>
<html>
	<head>
		<title>Recherche des filmes</title>
		<link rel="stylesheet" type="text/css"  href="ws.css">
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.3/css/tether-theme-arrows-dark.min.css">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.3/js/tether.min.js"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css">
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js"></script>
	</head>
	<body>
		<nav class="navbar navbar-dark bg-dark justify-content-between w-100 flex">
			<h1 class="header">Projet de Web Semantique</h1>
			<form action="ServerServlet" method="post">
				<input class="form-control mr-sm-2" type="text" placeholder="Look for a movie" aria-label="Search" name="query" id="query" autofocus>
				<input type="submit" value="Go" />
			</form>
      
		</nav>
		
		<div class="result container w-100 h-100 pt-2 flex">
			<div class="image inline">
				<img src = "https://pcpartpicker.com/static/forever/img/no-image.png" />
			</div>
			<div class="container" id="title">
				<h3>Title</h3>
			</div>
		</div>		
        
            <p> ${name} <p>
            <p> ${googleList} <p>
	</body>
</html>