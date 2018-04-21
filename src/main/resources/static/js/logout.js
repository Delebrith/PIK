app.controller('logoutController', function($scope, $cookies, $window) {
	$scope.logout = function() {
		$cookies.remove('token');
		$window.location.reload();
	}
});