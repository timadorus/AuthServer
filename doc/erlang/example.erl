%%
%% Demonstrates how to process the auth-token which is sent by a client as
%% part of the LOGIN process.
%%
%% author
%%  Torben K�nke
%%
-module(example).
-import(pbkdf2, [pbkdf2/4]).

%% ====================================================================
%% API functions
%% ====================================================================
-export([start/0]).

%%
%% The entry-point.
%%
start() ->
	% Sample auth-token without AES session-key, generated by authentication server.
%	AuthToken = "YJhiPHdL864lBNehdvDjxL19Ubiz27351PZL4K2P1re4Ffs3DjvSvj2orxzQ39idfznD8+G/0DnkygkdbL7x4dNyH3+9nNlZgfz3",

	% Sample auth-token with AES session-key, generated by authentication server.
	AuthToken = "ruQutHymdowtmr5+c5LSHTKhMV6h6bimNAOFNUKfoKzSSQNdkcJ/TLC4HVkt5AO5fk+io32tkoiYeLCFtfIVstSsL3mbWJcal/txmClVUPInc1yPmDr7xbZMKd+L26UZtLBJapop",

	verify_auth_token(AuthToken)
	% TODO: The gameserver should cache recent auth-tokens and reject auth-tokens
	%       if they are already in the recent-token-cache to prevent
	%       replay-attacks.
.

%%
%% verify_auth_token - Verifies the specified auth-token.
%%
%% AuthToken
%%  The auth-token to verify.
%% Return
%%  true if the auth-token is valid; Otherwise false.
%%
verify_auth_token(AuthToken) ->
	% Configuration values.
	% In reality, these should be stored in a configuration file and _must_ be
	% in sync with the corresponding values of the authentication server!
	{SharedSecretKey, SaltSize, IVSize, Iterations, AESKeySize, Transform} =
		{"SuperGeheim", 24, 16, 1000, 16, aes_ctr},
	% 1. Base64-decode the auth-token.
	Data = base64:decode(AuthToken),
	% 2. Extract the prepended salt and IV from the data.
	<<Salt:SaltSize/binary, IV:IVSize/binary, Rest/binary>> = Data,
	% 3. Derive the encryption key from the shared secret-key and the salt.
	{ok, AESKey} = pbkdf2:pbkdf2(sha, SharedSecretKey, Salt, Iterations, AESKeySize),
	% 4. Decrypt the data.
	State = crypto:stream_init(Transform, AESKey, IV),
	{_, DecryptedBinary} = crypto:stream_decrypt(State, Rest),
	DecryptedAuthToken = binary:bin_to_list(DecryptedBinary),
	% DecryptedAuthToken is the decrypted auth-token and is of the form
	%   User:Entity:Timestamp:Hostname
	% if session-encryption is not being used. If session-encryption is
	% being used, the decrypted auth-token is of the form
	%   User:Entity:Timestamp:Hostname:SessionKey
	%
	% 5. Split token into its individual parts.
	Parts = string:tokens(DecryptedAuthToken, ":"),
	[User, Entity, Ts, Hostname] = lists:sublist(Parts, 4),
	% 6. TODO: Verify User and Entity are actually the same as the ones sent
	%          by the client as part of the LOGIN message.
	% .... 
	% ....
	io:format(["Auth-Token components: User='", User, "', Entity='", Entity
			  ,"', Timestamp='", Ts ,"', Hostname='", Hostname, "'\n"]),
	% 7. Extract AES session-key if it's part of the ticket.
	case length(Parts) of
		5 ->
			SKey = lists:nth(5, Parts),
			io:format(["AES session-key (base64-encoded) for session-encryption: ",
						SKey, "\n"])
		;
		_ -> undefined
	end,
	% 8. Verify the hostname, i.e. verify that the hostname can be resolved to
	%    one of the machine's network interfaces.
	true = is_valid_hostname(Hostname),
	% 9. Verify timestamp is within a reasonable threshold.
	%    TODO: Find a good threshold value for the maximum lifetime of a token.
	{Timestamp, _} = string:to_integer(Ts),
	CurrentTime = get_unix_time(),
	DiffTime = CurrentTime - Timestamp,
	if
		% Timestamp is older than 100 seconds.
		DiffTime > 100 ->
			io:format(["Auth-Token is valid, but the timestamp is too old\n"]),
			false
		;
		true ->
			true
	end	
.

%
% Determines whether the specified hostname can be resolved to an address of one
% of the machine's network interfaces.
%
% Hostname
%  The hostname to verify.
% Return
%  true if the hostname is valid; Otherwise false.
%
is_valid_hostname(Hostname) ->
%	{ok, Iflist} = inet:getifaddrs(),
	% TODO: Iterate over addresses of all network interfaces in Iflist and determine
	% whether any equals Hostname (or the address Hostname has been resolved to).
	Hostname,
	true
.

%
% Returns the current time as the number of seconds that have passed since
% 01.01.1970.
%
get_unix_time() ->
	{Mega, Secs, _} = now(),
	Mega * 1000000 + Secs
.
