var t
app.controller('projectController', function($scope, $http, $cookies, $window, $location) {
	$scope.teachers = []
	$scope.projects = []
	$scope.participatingUsers = []
	
	/////////////////////////general utils/////////////////////////
	
	$scope.statusToString = function(status) {
		if (status == undefined)
			return ""
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
	$scope.participationStatusesToString = function(pstatuses) {
		var ret = "";
		if (pstatuses != undefined)
			for (i = 0; i < pstatuses.length; i++)
				ret += (ret == "" ? "" : ", ") + participationStatusToString(pstatuses[i])
				
		return ret
	}
	
	function participationStatusToString(pstatus) {
		pstatus = pstatus.status
		if (pstatus == undefined)
			return "Trwa ładowanie..."
		if (pstatus.toUpperCase() == "GRADUATE_WORK")
			return "Praca dyplomowa"
		if (pstatus.toUpperCase() == "PARTICIPANT")
			return "Uczestnik"
		if (pstatus.toUpperCase() == "OWNER")
			return "Właściciel"
		if (pstatus.toUpperCase() == "MANAGER")
			return "Opiekun"
		if (pstatus.toUpperCase() == "PENDING_INVITATION")
			return "Oczekuje zaproszenie"
		if (pstatus.toUpperCase() == "WAITING_FOR_ACCEPTANCE")
			return "Oczekuje na przyjęcie"
	}

	$scope.getProjectPayString = function(project) {
		if (project == undefined)
			return ""
		if (project.minimumPay == project.maximumPay)
			return project.minimumPay
		else
			return project.minimumPay + " - " + project.maximumPay
	}
	
	/////////////////////////adding project/////////////////////////
	
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

	/////////////////////////projects lists/////////////////////////
	
	function getMyProjectsHref(page, statuses = undefined) {
		href = "/?page=" + page
		if (statuses != undefined)
		{
			if (Array.isArray(statuses))
				for (i = 0; i < statuses.length; i++)
					href += "&status=" + statuses[i]
			else
				href += "&status=" + statuses
		}
		
		href += "#my-projects"
			
		return href
	}
	
	function getSearchProjectsHref(query, page, minEcts, minPay, onlyGradWork, statuses = undefined) {
		if (page == undefined)
			page = 0
		if (query == undefined)
			query = ""
		if (onlyGradWork == undefined)
			onlyGradWork = false
		href = "/?page=" + page + "&query=" + encodeURIComponent(query)
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
		
	$scope.filterProjects = function() {
		var statuses = []
		if ($scope.projectSearchCreated)
			statuses.push("CREATED");
		if ($scope.projectSearchWaiting)
			statuses.push("WAITING_FOR_STUDENTS");
		if ($scope.projectSearchAllStarted) {
			statuses.push("STARTED");
			statuses.push("SUSPENDED_MISSING_TEACHER");
			statuses.push("SUSPENDED_MISSING_PARTICIPANTS");
			statuses.push("OVERDUE");
		}
		if ($scope.projectSearchStarted)
			statuses.push("STARTED");
		if ($scope.projectSearchMissingTeacher)
			statuses.push("SUSPENDED_MISSING_TEACHER");
		if ($scope.projectSearchMissingParticipants)
			statuses.push("SUSPENDED_MISSING_PARTICIPANTS");
		if ($scope.projectSearchOverdue)
			statuses.push("OVERDUE");
		if ($scope.projectSearchFinished)
			statuses.push("FINISHED");
		if ($scope.projectSearchCanceled)
			statuses.push("CANCELED");
		if ($scope.projectSearchReported)
			statuses.push("SUSPENDED_REPORTED");

		if ($scope.isPage($scope.pages.projectSearchPanel))
			$window.location.href = getSearchProjectsHref(
					$scope.params.query,
					$scope.params.page,
					$scope.projectSearchECTS,
					$scope.projectSearchMinPay,
					$scope.projectSearchGradWorkOnly,
					statuses)
		
		else if ($scope.isPage($scope.pages.myProjects))
			$window.location.href = getMyProjectsHref(
					$scope.params.page,
					statuses)
	}
	function prepareProjectList(){
		if ($scope.isLogged() && (
				$scope.isPage($scope.pages.projectSearchPanel) ||
				$scope.isPage($scope.pages.myProjects))) {
			if (!$scope.params.page)
				$scope.params.page = 0
			
			var href
			if ($scope.isPage($scope.pages.projectSearchPanel)) {
				if (!$scope.params.query)
					$scope.params.query = ""
						
				href = "/project/find/20/" + $scope.params.page + "?query=" + $scope.params.query
	
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
			}
			else {
				href = "/project/my/20/" + $scope.params.page
	
				if ($scope.params.status != undefined)
				{
					if (Array.isArray($scope.params.status))
						for (i = 0; i < $scope.params.status.length; i++)
							href += (i == 0 ? "?" : "&") + "status=" + $scope.params.status[i]
					else
						href += "?status=" + $scope.params.status
				}			
			}
	
			var response = $http.get(href)
			response.then(
			    	function(response) {
						$scope.projects = response.data
						
						var projectsById = {}
						for (i = 0; i < $scope.projects.length; i++)
							projectsById["_" + $scope.projects[i].id] = $scope.projects[i]
						
						t = projectsById
						if ($scope.isPage($scope.pages.myProjects))
							for (i = 0; i < $scope.projects.length; i++)
							{
								$http.get("/participation/project/" + $scope.projects[i].id + "/user/" + $scope.context.user.email)
									.then(function(response) {
										if (response.data.length > 0)
											projectsById["_" + response.data[0].project.id].participationStatus = response.data
									})
							}
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
					if ($scope.params.status[i].toUpperCase() == "STARTED") {
						$scope.projectSearchAllStarted = $scope.isPage($scope.pages.projectSearchPanel)
						$scope.projectSearchStarted = $scope.isPage($scope.pages.myProjects)
					}
					if ($scope.params.status[i].toUpperCase() == "SUSPENDED_MISSING_TEACHER") {
						$scope.projectSearchAllStarted = $scope.isPage($scope.pages.projectSearchPanel)
						$scope.projectSearchMissingTeacher = $scope.isPage($scope.pages.myProjects)
					}
					if ($scope.params.status[i].toUpperCase() == "SUSPENDED_MISSING_PARTICIPANTS") {
						$scope.projectSearchAllStarted = $scope.isPage($scope.pages.projectSearchPanel)
						$scope.projectSearchMissingParticipants= $scope.isPage($scope.pages.myProjects)
					}
					if ($scope.params.status[i].toUpperCase() == "OVERDUE") {
						$scope.projectSearchAllStarted = $scope.isPage($scope.pages.projectSearchPanel)
						$scope.projectSearchOverdue = $scope.isPage($scope.pages.myProjects)
					}
					if ($scope.params.status[i].toUpperCase() == "FINISHED")
						$scope.projectSearchFinished = true
					if ($scope.params.status[i].toUpperCase() == "CANCELED")
						$scope.projectSearchCanceled = true
					if ($scope.params.status[i].toUpperCase() == "SUSPENDED_REPORTED")
						$scope.projectSearchReported = true
				}
			}
	
			$scope.projectSearchQuery = decodeURIComponent($scope.params.query)
			$scope.projectSearchECTS = parseInt($scope.params['min-ects'])
			$scope.projectSearchMinPay = parseInt($scope.params['min-pay'])
			$scope.projectSearchGradWorkOnly = $scope.params['only-grad-work'] == "true"
		}
	}
	$scope.$on('logged-in', function (event,data) { prepareProjectList() });
	prepareProjectList()
	
	$scope.hasParticipationStatus = function(participations, status) {
		if (participations == undefined)
			return undefined;
		
		for (var i = 0; i < participations.length; i++) {
			if (participations[i].status == status)
				return true;
		}
		return false;
	}

	/////////////////////////project management/////////////////////////
	
	function getParticipatingUsers(participations) {
		var map = {}
		
		for(i = 0; i < participations.length; i++) {
			if (map[participations[i].user.email] == undefined)
				map[participations[i].user.email] = { user: participations[i].user, participationStatuses: [ {status: participations[i].status} ] }
			else
				map[participations[i].user.email].participationStatuses.push([{ status: participations[i].status} ])
		}
		
		var ret = []
		for (u in map)
			if (map.hasOwnProperty(u))
				ret.push(map[u])
				
		return ret
	}
	function prepareProjectToManage() {
		if ($scope.isPage($scope.pages.projectManagementPanel)){
			var currentProjectHref = "/project/" + $scope.params.project
			$http.get(currentProjectHref).then(function(response) {
				$scope.managedProject = response.data
	
				$http.get(
						"/participation/project/" + $scope.managedProject.id + "/user/" + $scope.context.user.email
						).then(function(response) {
							$scope.managedProject.participations = response.data
                            $scope.modifiedIsGraduateWord = $scope.managedProject.isGraduateWork
						},function(response) {
							$scope.managedProject.participations = []
						})
	
				$http.get(
						"/participation/project/" + $scope.managedProject.id
						).then(function(response) {
							$scope.participatingUsers = getParticipatingUsers(response.data)
						},function(response) {
							$scope.participatingUsers = []
						})
			})			
		}
	}
	$scope.$on('logged-in', function(event,data) { prepareProjectToManage() })
	prepareProjectToManage();

	$scope.statusesOfParticipating = function(user) {
		var statuses = []
		for (i = 0; i < $scope.participatingUsers.length; i++)
			if ($scope.participatingUsers[i].user.email == user.email)
				statuses.push($scope.participatingUsers[i].status)
		
		return statuses
	}

	$scope.changeProject = function() {
        var changeProjectUrl = "/project/change/" + $scope.managedProject.id + "?";
        if ($scope.modifiedName)
            changeProjectUrl += "name=" + $scope.modifiedName + "&";
            $scope.modifiedName = undefined;
        if ($scope.modifiedDescription)
            changeProjectUrl += "description=" + $scope.modifiedDescription + "&";
            $scope.modifiedDescription = undefined;
        if ($scope.modifiedNumOfParticipants)
            changeProjectUrl += "numOfParticipants=" + $scope.modifiedNumOfParticipants + "&";
            $scope.modifiedNumOfParticipants = undefined;
        if ($scope.modifiedMinimumPay)
            changeProjectUrl += "minimumPay=" + $scope.modifiedMinimumPay + "&";
            $scope.modifiedMinimumPay = undefined;
        if ($scope.modifiedMaximumPay)
            changeProjectUrl += "maximumPay=" + $scope.modifiedMaximumPay + "&";
            $scope.modifiedMaximumPay = undefined;
        if ($scope.modifiedEcts)
            changeProjectUrl += "ects=" + $scope.modifiedEcts + "&";
            $scope.modifiedEcts = undefined;
        if ($scope.modifiedIsGraduateWork)
            changeProjectUrl += "isGraduateWork=" + $scope.modifiedIsGraduateWork + "&";
            $scope.modifiedIsGraduateWork = undefined;
        var response = $http.post(changeProjectUrl)
        response.then(
            function (response) {
                alert("Zmodyfikowano projekt")
                prepareProjectToManage()
            }, function () {
                alert("Modyfikacja projektu nie powiodła się")
            }
        );
    }

    $scope.changeStatus = function(id, status) {
        var projectChangeUrl = "/project/" + id + "/changeStatus" + "?status=" + status;
        var response = $http.post(projectChangeUrl)
        response.then(
            function (response) {
                alert("Zmodyfikowano status")
                prepareProjectToManage()
            }, function (response) {
                alert("Modyfikacja statusu nie powiodła się")
            }
        );
	}

	$scope.changeParticipation = function (status, projectId, username) {
        var participationChangeUrl = "/participation/change"
        var requestBody = {
            status: status,
            projectId: projectId,
            username: username
        }
        var response = $http.post(participationChangeUrl, requestBody)
        response.then(
            function (response) {
                alert("Zmieniono status uczestnictwa")
                prepareProjectToManage()
            }, function (response) {
                alert("Zmiana statusu uczestnictwa nie powiodła się")
            }
        );
    }

});