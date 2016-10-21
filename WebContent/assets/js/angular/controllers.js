'use strict';

app.controller("MainCtrl", function($scope, $http) {
	$scope.showModal = false;

	$scope.toggleModal = function() {
		$scope.showModal = !$scope.showModal;
	};

	$scope.submitDwForm = function(name, email) {
		window.open("/AndroidRsaServer/api/download?ownerName=" + name
				+ "&email=" + email, '_blank', '');
	};

	$scope.submitSupportForm = function() {
		$http.post("/AndroidRsaServer/api/support", $scope.supportForm).then(
				function successCallback(response) {
					$('.contact-success').fadeIn().delay(3000).fadeOut();
				}, function errorCallback(response) {
					$('.contact-error').fadeIn().delay(3000).fadeOut();
				});
	};
});
