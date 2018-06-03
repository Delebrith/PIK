var sc;

app.controller('adminController', function($scope, $http, $cookies, $window, $location) {
    sc = $scope;
    $scope.users;
    $scope.authorities;
    $scope.checkedAuthorities = {};

    $scope.getNextUsersPage = function(project) {
        return getSearchProjectsHref(
            $scope.params.name,
            parseInt($scope.params.page) + 1,
            $scope.params.authorities)
    }

    $scope.getPrevUsersPage = function(project) {
        return getSearchProjectsHref(
            $scope.params.name,
            parseInt($scope.params.page) - 1,
            $scope.params.authorities)
    }
    
    $scope.searchUsers = function() {
    	$window.location.href = getSearchUsersHref(
                $scope.userSearchQuery,
                parseInt($scope.params.page),
                $scope.params.authorities)
    }
    
    function getSearchUsersHref(name, page, authList = undefined) {
        if (page == undefined)
            page = 0
        if (name == undefined)
            name = ""

        href = "?page=" + page + "&name=" + encodeURIComponent(name)

        if (authList != undefined)
        {
            if (Array.isArray(authList))
                for (i = 0; i < authList.length; i++)
                    href += "&authorities=" + authList[i]
            else
                href += "&authorities=" + authList
        }

        href += "#user-list"

        return href
    }

    $scope.filterUsers = function() {
        var authList = []
        for (var key in $scope.checkedAuthorities) {
            if ($scope.checkedAuthorities[key]) {
                authList.push(key);
            }
        }

        $window.location.href = getSearchUsersHref(
            $scope.params.name,
            $scope.params.page,
            authList)

    }
    
    $scope.deleteUser = function(user) {
    	if (confirm("Usuwanie użytkownika: " + user.name + "\nCzy jesteś pewien że chcesz kontynuować?"))
    		$http.post("/user/" + user.id + "/delete").then(function(response) {
    			alert("Użytkownik został usunięty")
    			location.reload();
    		}, function(response) {
    			alert("Nie udało się wykonać operacji")
    		})
    }
    
    $scope.manageUser = function(user) {
    	$window.location.href = "/?user=" + user.id + "#user-management"
    }
    
    function getAuthorities() {
    	if($scope.isLogged) {
	    	var authoritiesUrl = "/user/authorities";
		    var response = $http.get(authoritiesUrl)
		    response.then(
		        function(response) {
		            $scope.authorities = response.data
		        },
		        function(response){
		                alert("Wystąpił błąd podczas pobierania ról.")
		        });
    	}
    }
    
    function searchForUsers() {
    	if ($scope.isLogged() && $scope.isPage($scope.pages.userList)) {
		    var usersUrl = "/user/filterAndFind/20/";
	
		    if ($scope.isLogged() && $scope.isPage($scope.pages.userList)) {
		        if ($scope.params.page == undefined)
		        	$scope.params.page = 0
		        var href = usersUrl + $scope.params.page + "?"
		
		        if ($scope.params.authorities != undefined) {
		            if (Array.isArray($scope.params.authorities))
		                for (i = 0; i < $scope.params.authorities.length; i++)
		                    href += "&authorities=" + $scope.params.authorities[i]
		            else
		                href += "&authorities=" + $scope.params.authorities
		        }
		        
		        if ($scope.params['name'])
		            href += "&name=" + $scope.params['name']
	
		        var response = $http.get(href)
		        response.then(
		            function (response) {
		                $scope.users = response.data
		            },
		            function (response) {
		                alert("Wystąpił błąd podczas pobierania użytkowników.")
		            });
		
		
		        if ($scope.params.authorities != undefined) {
		            if (!Array.isArray($scope.params.authorities))
		                $scope.params.authorities = [$scope.params.authorities]
		
		            for (var key in $scope.params.authorities) {
		                $scope.checkedAuthorities[$scope.params.authorities[key]] = true;
		            }
		        }
		    }
	    }
    	
    }
    $scope.userSearchQuery = $scope.params.name
    $scope.$on('logged-in', function(event, data) { searchForUsers() })
    searchForUsers()
    $scope.$on('logged-in', function(event, data) { getAuthorities() })
    getAuthorities()
    
    
    $scope.managedUser = {}
    function getManagedUser() {
    	if ($scope.isLogged() && $scope.isPage($scope.pages.userManagementPanel)) {
    		$http.get("/user/" + $scope.params.user + "/find").then(function(response) {
    			$scope.managedUser = response.data
    			$scope.modifiedName = $scope.managedUser.name
    			$scope.modifiedEmail = $scope.managedUser.email
    			$scope.modifiedPhoneNo = $scope.managedUser.phoneNo
    		})
    	}
    }

    $scope.$on('logged-in', function(event, data) { getManagedUser() })
    getManagedUser()
    
    function updateUser() {
    	$http.post("/user/modify", $scope.managedUser).then(function(response) {
    	}, function(response) {
    		alert("Nie udało się zmodyfikować użytkownika!")
    	})
    }
    
    $scope.modifyUserName = function(){
    	if ($scope.modifiedName == undefined)
    		alert("Wprowadź nazwę!")
    	
    	$scope.managedUser.name = $scope.modifiedName
    	
    	updateUser();
    }
    
    $scope.modifyUserEmail = function(){
    	if ($scope.modifiedEmail == undefined)
    		alert("Wprowadź email!")

        $scope.managedUser.email = $scope.modifiedEmail

    	updateUser();
    }
    
    $scope.modifyUserPhoneNo = function(){
    	$scope.managedUser.phoneNo = $scope.modifiedPhoneNo

    	updateUser();
    }
    
    function getAuthority(name) {
    	for(i = 0; i < $scope.authorities.length; i++)
    		if ($scope.authorities[i].name == name)
    			return $scope.authorities[i];
    }
    
    $scope.submitUser = function(){
    	errors = ""
    	if (!$scope.created.name)
    		errors += "Wprowadź nazwę!"
    	if (!$scope.created.email)
    		errors += "Wprowadź adres email!"
    	$scope.created.authorities = []

    	if ($scope.createdRoles.ADMIN)
    		$scope.created.authorities.push(getAuthority('ADMIN'))
    	if ($scope.createdRoles.AUTHORITY)
    		$scope.created.authorities.push(getAuthority('AUTHORITY'))
        if ($scope.createdRoles.STUDENT)
        	$scope.created.authorities.push(getAuthority('STUDENT'))
        	
    	if ($scope.createdRoles.TEACHER)
    		$scope.created.authorities.push(getAuthority('TEACHER'))
    	if ($scope.createdRoles.THIRD_PARTY)
    		$scope.created.authorities.push(getAuthority('THIRD_PARTY'))
		if ($scope.createdRoles.TEACHER || $scope.createdRoles.THIRD_PARTY)
			$scope.created.authorities.push(getAuthority('EMPLOYER'))
		
		$http.post("/user/create", $scope.created).then(function(response) {
			alert("Użytkownik został dodany")
			location.reload();
		}, function(response) {
			alert("Nie udało się dodać użytkownika")
		})
    }
});