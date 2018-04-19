function invalidEmail()
{
	alert('Niepoprawny adres email!');
}

function failedLogin(response)
{
	alert('Nieudane logowanie!');
}

function succesfulLogin(response)
{
	alert('Udane logowanie');
}

app.controller('loginController', function($scope, $http) {
	$scope.submit = function()	{
		var userCredentialsDto = {
			email: $scope.email,
			password: $scope.password
		};
		
		if ($scope.email == undefined) {
			invalidEmail();
			return;
		}
		
	    var response = $http.post("login", userCredentialsDto);
	    $scope.myWelcome = response.then(
	    		function(response) {
					succesfulLogin(response);
	    		},
	    		function(response){
					failedLogin(response);	
	    		});
	}
});