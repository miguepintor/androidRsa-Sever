'use strict';

app.controller("MainCtrl", function($scope, $http) {
	$scope.showModal = false;
	
	$scope.toggleModal = function() {
		$scope.showModal = !$scope.showModal;
	};
	
	$scope.submitForm = function() {
		window.open("/AndroidRsaServer/api/download?ownerName=" + $scope.name, '_blank', '');
	
	};
});
		