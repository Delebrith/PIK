var sc;

app.controller('adminController', function($scope, $http, $cookies, $window, $location) {
    sc = $scope;
    $scope.users;
    $scope.authorities;
    $scope.checkedAuthorities = {};

    $scope.getNextUsersPage = function(project) {
        return getSearchProjectsHref(
            $scope.params.query,
            parseInt($scope.params.page) + 1,
            $scope.params.status)
    }

    $scope.getPrevUsersPage = function(project) {
        return getSearchProjectsHref(
            $scope.params.query,
            parseInt($scope.params.page) - 1,
            $scope.params.status)
    }

    function getSearchProjectsHref(name, page, minEcts, minPay, onlyGradWork, authList = undefined) {
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

        $window.location.href = getSearchProjectsHref(
            $scope.params.name,
            $scope.params.page,
            authList)

    }

    var authoritiesUrl = "/user/authorities";
    var response = $http.get(authoritiesUrl)
    response.then(
        function(response) {
            $scope.authorities = response.data
            console.log($scope.authorities);
        },
        function(response){
                alert("Wystąpił błąd podczas pobierania ról.")
        });

    var usersUrl = "/user/filterAndFind/20/";
    alert($scope.isLogged() +", "+ $scope.isPage($scope.pages.userList) + ", " + $scope.context.user)
    if ($scope.isLogged() && $scope.isPage($scope.pages.userList)) {
        alert(2)
        var href = usersUrl + $scope.params.page

        if ($scope.params.authorities != undefined) {
            if (Array.isArray($scope.params.authorities))
                for (i = 0; i < $scope.params.authorities.length; i++)
                    href += "&authorities=" + $scope.params.authorities[i]
            else
                href += "&authorities=" + $scope.params.authorities
        }
        if ($scope.params['name'] != undefined)
            href += "&name=" + $scope.params['name']
        alert(href)
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

});