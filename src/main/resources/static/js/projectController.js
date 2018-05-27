app.controller('projectController', function($scope, $http, $cookies, $window) {
	function validateProject() {
		errors = ""
			
		if ($scope.name != undefined)
			$scope.name = $scope.name.trim()
		
		if ($scope.name == undefined || $scope.name.length == 0)
			errors += "Nazwa projektu nie może być pusta!\n"
		
		if ($scope.description == undefined || $scope.description.length == 0)
			errors += "Opis projektu nie może być pusty!\n"
		
		if ($scope.participants == undefined)
			errors += "Projekt musi mieć ustawioną liczbę uczestników!\n"
		
		if ($scope.isUser3rdParty())
		{
			if ($scope.minimumPay != undefined &&
				$scope.maximumPay != undefined &&
				$scope.minimumPay > $scope.maximumPay)
				errors += "Kwota minimalna nie może być większa od maksymalnej!\n"
		}
		
		if (errors == "")
			return true
		
		alert(errors)
		return false;
	}
	
	$scope.submitAdd = function() {
		if (!validateProject())
			return;
	}
})