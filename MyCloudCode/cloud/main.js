
// Use Parse.Cloud.define to define as many cloud functions as you want.

Parse.Cloud.define("addGameToUser", function(request, response) {
	Parse.Cloud.useMasterKey();
	var userQuery = new Parse.Query(Parse.User);
	userQuery.get(request.params.userId, {
		success: function(user) {
			var gameQuery = new Parse.Query("Game");
			gameQuery.get(request.params.gameId, {
				success: function(game) {
					user.relation("games").add(game);
					user.save();
					response.success("Game successfuly added to player's relation.");
				},
				error:  function(error) {
					response.error("Error adding game to user's relation.");
				}
			});
		},
		error:  function(error) {
			response.error("Error adding game to user's relation.");
		}
	});
});

Parse.Cloud.define("removeGameFromUser", function(request, response) {
	Parse.Cloud.useMasterKey();
	var userQuery = new Parse.Query(Parse.User);
	userQuery.get(request.params.userId, {
		success: function(user) {
			var gameQuery = new Parse.Query("Game");
			gameQuery.get(request.params.gameId, {
				success: function(game) {
					user.relation("games").remove(game);
					user.save();
					response.success("Game successfuly removed from player's relation.");
				},
				error:  function(error) {
					response.error("Error removing game from user's relation.");
				}
			});
		},
		error:  function(error) {
			response.error("Error removing game from user's relation.");
		}
	});
});