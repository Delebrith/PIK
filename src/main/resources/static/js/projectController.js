var tresp
app.controller('projectController', function($scope, $http, $cookies, $window) {
	$scope.teachers = []
	
	$scope.getTeachers = function(typed) {
		if (typed == undefined || typed == "")
			return;
		
		$scope.getUsersByRoleAndName("TEACHER", typed, 5, function(response) {
			if (response == undefined)
				return
				
			tresp = response
			teachers = []
			for (var i = 0; i < response.data.length; i++)
				teachers.push(response.data[i].name + " [" + response.data[i].email + "]")

			$scope.teachers = teachers
		})
	}
	
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
			
			if ($scope.project.teacher != undefined)
			{
				emailBegin = $scope.project.teacher.indexOf('[')
				emailEnd = $scope.project.teacher.lastIndexOf(']')
				
				if (emailBegin == -1 || emailEnd == -1 || emailBegin >= emailEnd)
					errors += "Niepoprawny nauczyciel"
				$scope.project.teachersMail = $scope.project.teacher.substr(emailBegin + 1, emailEnd - emailBegin - 1)
			}
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
				$window.location.reload()
	    	},
	    	function(response){
	    		alert("Wystąpił błąd podczas dodawania projektu.")
    		});
	}
})