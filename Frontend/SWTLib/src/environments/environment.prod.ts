export const environment = {
  production: true,
  // oauth config
  oAuth_client_id: "242e1aa426023d6353dc38debc6f10eb671ea989cd5481e78017f31439254009",
  oAuth_url: "https://gitlab.rz.uni-bamberg.de/oauth/authorize",
  oAuth_response_type: "code",
  oAuth_scope: "openid%20email",
  // rest API config
  API_HOST: 'http://backend.master-swtlibrary-1315.runner.swt.uni-bamberg.de',
  API_PORT: 80,
  REDIRECT_HOST: 'http://master-swtlibrary-1315.runner.swt.uni-bamberg.de',
  REDIRECT_PORT: 80,
  // the names of the priorities of reservations
  priority: ['1_Very High', '2_High', '3_Medium', '4_Low', '5_Very Low']
};
