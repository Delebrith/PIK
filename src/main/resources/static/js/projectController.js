app.controller('projectController', function($scope, $http, $cookies, $route) {
	function validateProject() {
		errors = ""
			
		if ($scope.project.name != undefined)
			$scope.project.name = $scope.project.name.trim()
		
		if ($scope.project.name == undefined || $scope.project.name.length == 0)
			errors += "Nazwa projektu nie może być pusta!\n"
		
		if ($scope.project.description == undefined || $scope.project.description.length == 0)
			errors += "Opis projektu nie może być pusty!\n"
		
		if ($scope.project.participants == undefined)
			errors += "Projekt musi mieć ustawioną liczbę uczestników!\n"
		
		if ($scope.project.isGraduateWork == undefined)
			$scope.project.isGraduateWork = false
			
		if ($scope.isUser3rdParty())
		{
			if ($scope.project.minimumPay != undefined &&
				$scope.project.maximumPay != undefined &&
				$scope.project.minimumPay > $scope.project.maximumPay)
				errors += "Kwota minimalna nie może być większa od maksymalnej!\n"
		}
		
		if (errors == "")
			return true
		
		alert(errors)
		return false;
	}
	
	function addToDto(dto, dtoField, scopeField = dtoField) {
		if ($scope.project[scopeField] != undefined)
			dto[dtoField] = $scope.project[scopeField]
	} 
	
	$scope.submitCreateProject = function() {
		if (!validateProject())
			return;
		
		projectDto = {}

		addToDto(projectDto, "name")
		addToDto(projectDto, "numberOfParticipants", "participants")
		addToDto(projectDto, "description")
		addToDto(projectDto, "isGraduateWork")
		addToDto(projectDto, "ects")
		addToDto(projectDto, "minimumPay")
		addToDto(projectDto, "maximumPay")
		
		var response = $http.post("/project/add", projectDto);
	    response.then(
	    	function(response) {
				$route.reload()
	    	},
	    	function(response){
	    		alert("Wystąpił błąd podczas dodawania projektu.")
    		});
	}
})