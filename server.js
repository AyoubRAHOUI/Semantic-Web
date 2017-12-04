var express = require('express');
var session = require('cookie-session'); // Charge le middleware de sessions
var bodyParser = require('body-parser'); // Charge le middleware de gestion des paramètres
var urlencodedParser = bodyParser.urlencoded({ extended: false });
var requestify = require('requestify');
var xpath = require('xpath');
var dom = require('xmldom').DOMParser;
var QueryString = require('querystring');

var scraper = require('google-search-scraper');
 

var app = express();

const PORT = 8080;

/* On utilise les sessions */
app.use(session({secret: 'topsecret'}))

	// Dans une requête POST, les paramètres sont transmis dans le corps de la requête.
    // Avec express, pour extraire les paramètre du corps en paire clé/valeur, il faut utiliser un "body parser".
    // Il y a deux body parser : un pour extraire du json et l'autre pour traiter les requêtes post habituelles.
    .use(bodyParser.json())
    .use(bodyParser.urlencoded({extended: true}))


/* Méthode pour utiliser une session plus tard */
.use(function(req, res, next){
	// Ici, on peut faire un système de session qui donc persiste entre chaque page.
    // if (typeof(req.session.data) == 'undefined') {
    //     req.session.data = [];
    // }

    next();
})

/* On affiche la page HTML avec ejs*/
.get('/search', function(req, res) { 
    res.render('index.ejs');
})

/* On fait le POST */
.post('/search/new/', function(req, res) {

	//Si la recherche n'est pas vide
    if (req.body.query && req.body.query.length > 0) {
		
		
		var options = {
		  query: req.body.query,
		  limit: 10
		};
		var results = []
		
		scraper.search(options, function(err, url) {
		  // This is called for each result 
		  if(err) throw err;
		  results.push(url);
		  //console.log(url)
		})
		.then(function (response) {
			console.log(results)
			res.json({urls: results});
		});
		
		
		
		
        // Le replace va changer les espaces par des plus et les & par des &amp avec un regex
        var query = req.body.query.replace(/ /g, '+').replace(/&/g, '&amp;');

        requestify
        // Start est l'offset dans les résultats de recherche, pas le numéro de page
        // Pour récupérer la 2e page : start = 10 (car 10 resultats par pages)
            .get('https://www.google.fr/search?q='+ query +'&start=0')
            .then(function (response) {
                if (response.code === 200) {
                    // On doit transformer la chaîne en caractère en représentation en arbre du html/xml : le DOM
                    // Ensuite on pourra appliquer un xpath sur ce DOM.
                    var htmlAsDom = new dom().parseFromString(response.body);

                    // On récupère les URL des liens "titre" de google, les h3.
                    var xpathResult = xpath.select("//h3[@class='r']//a/@href", htmlAsDom);
					// Mais ces liens ne sont pas sains. Google ajoute une passerelle qu'il faut supprimer.
                    // Exemple : http://www.meninblack.com/
                    // -> /url?q=http://www.meninblack.com/&sa=U&ved=0ahUKEwihvImqs-zXAhVGMZoKHV5SBpQQFggUMAA&usg=AOvVaw3_TgNmT5x2s013HpVnoo0Q
                    // Dans la querystring (paramètres envoyés en fin d'url dans un GET par exemple), 
					// on doit récupérer la variable q ici pour avoir l'url

                    // xpath.select retourne un tableau d'objets complexes, appelés attributs
                    // Ces objets contiennent une variable, value, qui contient l'url pas propre que l'ont cherche à
                    // extraire et nettoyer.
                    // Le substr permet de virer le /url? et on utilise la librairie querystring de nodejs pour extraire
                    // la variable q de la querystring de chaque lien trouvé dans la page de google
                    var sanitizedValues = xpathResult.map(function(attribute) {

                        var queyrstring = QueryString.parse(attribute.value.substr(5));

                        return queyrstring.q;
                    });

                    // On enlève les cas où l'url est vide
                    sanitizedValues = sanitizedValues.filter(function (elem) {
                        return elem;
                        // Si la variable est undefined ou null, la valeur booléenne associée (donc son cast en booléen) est false.
                        // Donc les url vides (null ou undefined) seront exclues par le filtre.
                    });

                    // On renvoit un object qui va être parsé en json par express JS. On obtient les liens
                    res.json({success: true, urls: sanitizedValues});
                }
            });
			
    } else {
        res.json({success: false, error: "Invalid query parameters"});
    }
})


/* On redirige vers search si la page demandée n'est pas trouvée */
.use(function(req, res, next){
    res.redirect('/search');
})

.listen(PORT);

console.log("Serveur démarré sur le port " + PORT);