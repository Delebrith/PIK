app.controller('projectController', function($scope, $http, $cookies, $window, $location) {
	$scope.teachers = []
	$scope.projects = []
	
	$scope.statusToString = function(status) {
		if (status.toUpperCase() == "CREATED")
			return "Oczekuje na pracownika"
		if (status.toUpperCase() == "WAITING_FOR_STUDENTS")
			return "Oczekuje na studentów"
		if (status.toUpperCase() == "STARTED")
			return "Rozpoczęty"
		if (status.toUpperCase() == "SUSPENDED_MISSING_TEACHER")
			return "Wstrzymany (brak pracownika)"
		if (status.toUpperCase() == "SUSPENDED_MISSING_PARTICIPANTS")
			return "Wstrzymany (brak studentów)"
		if (status.toUpperCase() == "FINISHED")
			return "Zakończony"
		if (status.toUpperCase() == "OVERDUE")
			return "Opóźniony"
		if (status.toUpperCase() == "SUSPENDED_REPORTED")
			return "Zgłoszony"
		if (status.toUpperCase() == "CANCELED")
			return "Anulowany"
	}
	
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
		
		alert($scope.project.teachersMail)
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
	
	function getSearchProjectsHref(query, page, minEcts, minPay, onlyGradWork, statuses = undefined) {
		if (page == undefined)
			page = 0
		if (query == undefined)
			query = ""
		if (onlyGradWork == undefined)
			onlyGradWork = false
		href = "?page=" + page + "&query=" + encodeURIComponent(query)
		if (statuses != undefined)
		{
			if (Array.isArray(statuses))
				for (i = 0; i < statuses.length; i++)
					href += "&status=" + statuses[i]
			else
				href += "&status=" + statuses
		}
		if (minEcts != undefined)
			href += "&min-ects=" + minEcts
		if (minPay != undefined)
			href += "&min-pay=" + minPay
		if (onlyGradWork != undefined)
			href += "&only-grad-work=" + onlyGradWork
		
		href += "#project-search"
			
		return href
	}
	
	$scope.searchProjects = function() {
		href = getSearchProjectsHref($scope.projectSearchQuery, 0, 0, 0, false)
		$window.location.href = href
	}
	
	$scope.setSelectedProject = function(project) {
		$scope.selectedProject = project
	}

	$scope.getNextPage = function(project) {
		return getSearchProjectsHref(
				$scope.params.query,
				parseInt($scope.params.page) + 1,
				$scope.params['min-ects'],
				$scope.params['min-pay'],
				$scope.params['only-grad-work'],
				$scope.params.status)
	}
	$scope.getPrevPage = function(project) {
		return getSearchProjectsHref(
				$scope.params.query,
				parseInt($scope.params.page) - 1,
				$scope.params['min-ects'],
				$scope.params['min-pay'],
				$scope.params['only-grad-work'],
				$scope.params.status)
	}
	
	if ($scope.isLogged() && $scope.isPage($scope.pages.projectSearchPanel)) {
		if (!$scope.params.query)
			$scope.params.query = ""
				
		var href = "/project/find/20/" + $scope.params.page + "?query=" + $scope.params.query

		if ($scope.params.status != undefined)
		{
			if (Array.isArray($scope.params.status))
				for (i = 0; i < $scope.params.status.length; i++)
					href += "&status=" + $scope.params.status[i]
			else
				href += "&status=" + $scope.params.status
		}
		if ($scope.params['min-ects'] != undefined)
			href += "&min-ects=" + $scope.params['min-ects']
		if ($scope.params['min-pay'] != undefined)
			href += "&min-pay=" + $scope.params['min-pay']
		if ($scope.params['only-grad-work'] != undefined)
			href += "&only-grad-work=" + $scope.params['only-grad-work']

		var response = $http.get(href)
		response.then(
		    	function(response) {
					$scope.projects = response.data
		    	},
		    	function(response){
		    		alert("Wystąpił błąd podczas pobierania projektów.")
	    		});
		

		if ($scope.params.status != undefined)
		{
			if (!Array.isArray($scope.params.status))
				$scope.params.status = [$scope.params.status]
			
			for (i = 0; i < $scope.params.status.length; i++) {
				if ($scope.params.status[i].toUpperCase() == "CREATED")
					$scope.projectSearchCreated = true
				if ($scope.params.status[i].toUpperCase() == "WAITING_FOR_STUDENTS")
					$scope.projectSearchWaiting = true
				if ($scope.params.status[i].toUpperCase() == "STARTED")
					$scope.projectSearchStarted = true
				if ($scope.params.status[i].toUpperCase() == "SUSPENDED_MISSING_TEACHER")
					$scope.projectSearchStarted = true
				if ($scope.params.status[i].toUpperCase() == "SUSPENDED_MISSING_PARTICIPANTS")
					$scope.projectSearchStarted = true
				if ($scope.params.status[i].toUpperCase() == "OVERDUE")
					$scope.projectSearchStarted = true
				if ($scope.params.status[i].toUpperCase() == "FINISHED")
					$scope.projectSearchFinished = true
				if ($scope.params.status[i].toUpperCase() == "CANCELED")
					$scope.projectSearchCanceled = true
				if ($scope.params.status[i].toUpperCase() == "SUSPENDED_REPORTED")
					$scope.projectSearchReported = true
			}
		}

		$scope.projectSearchQuery = $scope.params.query
		$scope.projectSearchECTS = parseInt($scope.params['min-ects'])
		$scope.projectSearchMinPay = parseInt($scope.params['min-pay'])
		$scope.projectSearchGradWorkOnly = $scope.params['only-grad-work'] == "true"
	}
	
	$scope.filterProjects = function() {
		var statuses = []
		if ($scope.projectSearchCreated)
			statuses.push("CREATED");
		if ($scope.projectSearchWaiting)
			statuses.push("WAITING_FOR_STUDENTS");
		if ($scope.projectSearchStarted) {
			statuses.push("STARTED");
			statuses.push("SUSPENDED_MISSING_TEACHER");
			statuses.push("SUSPENDED_MISSING_PARTICIPANTS");
			statuses.push("OVERDUE");
		}
		if ($scope.projectSearchFinished)
			statuses.push("FINISHED");
		if ($scope.projectSearchCanceled)
			statuses.push("CANCELED");
		if ($scope.projectSearchReported)
			statuses.push("SUSPENDED_REPORTED");

		$window.location.href = getSearchProjectsHref(
				$scope.params.query,
				$scope.params.page,
				$scope.projectSearchECTS,
				$scope.projectSearchMinPay,
				$scope.projectSearchGradWorkOnly,
				statuses)
	}
})