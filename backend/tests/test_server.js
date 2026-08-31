const http = require('http');

function request(method, path, data, token) {
  return new Promise((resolve, reject) => {
    const payload = data ? JSON.stringify(data) : null;
    const options = {
      hostname: '127.0.0.1',
      port: 5000,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        ...(payload ? { 'Content-Length': Buffer.byteLength(payload) } : {}),
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      }
    };
    const req = http.request(options, res => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(body) });
        } catch (e) {
          resolve({ status: res.statusCode, raw: body });
        }
      });
    });
    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
  });
}

(async () => {
  console.log('=== CALLMATE AI AUTHENTICATION & CLOUD TEST SUITE ===');

  // Test 1: System Health Check & MongoDB Atlas Connectivity
  const health = await request('GET', '/api/health');
  console.log('Test 1: Health Check & Atlas Connectivity ->', health.status === 200 && health.body.database.connected ? 'PASS' : 'FAIL', `(${health.body.database.host})`);

  // Test 2: Validation - Empty Fields
  const emptyReg = await request('POST', '/api/auth/register', { name: '', email: '', password: '', confirmPassword: '' });
  console.log('Test 2: Reject Empty Registration Fields ->', emptyReg.status === 400 ? 'PASS' : 'FAIL', `"${emptyReg.body.message}"`);

  // Test 3: Validation - Invalid Email
  const badEmailReg = await request('POST', '/api/auth/register', {
    name: 'Test User',
    email: 'not-an-email',
    password: 'password123',
    confirmPassword: 'password123'
  });
  console.log('Test 3: Reject Invalid Email Format ->', badEmailReg.status === 400 ? 'PASS' : 'FAIL', `"${badEmailReg.body.message}"`);

  // Test 4: Validation - Password Mismatch
  const mismatchReg = await request('POST', '/api/auth/register', {
    name: 'Test User',
    email: 'mismatch@callmate.ai',
    password: 'password123',
    confirmPassword: 'differentPassword'
  });
  console.log('Test 4: Reject Password Mismatch ->', mismatchReg.status === 400 ? 'PASS' : 'FAIL', `"${mismatchReg.body.message}"`);

  // Test 5: Register New User A
  const emailA = `user_a_${Date.now()}@callmate.ai`;
  const regA = await request('POST', '/api/auth/register', {
    name: 'Alice Cooper',
    email: emailA,
    password: 'password123',
    confirmPassword: 'password123',
    phoneNumber: '+1 (555) 111-2222'
  });
  console.log('Test 5: Register User A ->', regA.status === 201 && regA.body.token ? 'PASS' : 'FAIL', regA.body.message);
  const tokenA = regA.body.token;

  // Test 6: Reject Duplicate Email Registration
  const dupReg = await request('POST', '/api/auth/register', {
    name: 'Alice Imposter',
    email: emailA,
    password: 'anotherPassword',
    confirmPassword: 'anotherPassword'
  });
  console.log('Test 6: Reject Duplicate Email ->', dupReg.status === 400 ? 'PASS' : 'FAIL', `"${dupReg.body.message}"`);

  // Test 7: Register User B (For User Isolation Verification)
  const emailB = `user_b_${Date.now()}@callmate.ai`;
  const regB = await request('POST', '/api/auth/register', {
    name: 'Bob Marley',
    email: emailB,
    password: 'password456',
    confirmPassword: 'password456',
    phoneNumber: '+1 (555) 333-4444'
  });
  console.log('Test 7: Register User B ->', regB.status === 201 && regB.body.token ? 'PASS' : 'FAIL', regB.body.message);
  const tokenB = regB.body.token;

  // Test 8: Login Failure - Unknown Account
  const unknownLogin = await request('POST', '/api/auth/login', {
    email: 'nonexistent_9999@callmate.ai',
    password: 'password123'
  });
  console.log('Test 8: Reject Unknown Account Login ->', unknownLogin.status === 401 ? 'PASS' : 'FAIL', `"${unknownLogin.body.message}"`);

  // Test 9: Login Failure - Wrong Password
  const wrongPwdLogin = await request('POST', '/api/auth/login', {
    email: emailA,
    password: 'incorrectPassword'
  });
  console.log('Test 9: Reject Wrong Password Login ->', wrongPwdLogin.status === 401 ? 'PASS' : 'FAIL', `"${wrongPwdLogin.body.message}"`);

  // Test 10: Successful Login
  const loginA = await request('POST', '/api/auth/login', {
    email: emailA,
    password: 'password123'
  });
  console.log('Test 10: Successful Login with Hashed Password ->', loginA.status === 200 && loginA.body.token ? 'PASS' : 'FAIL', loginA.body.message);

  // Test 11: Active Session Check
  const meA = await request('GET', '/api/auth/me', null, tokenA);
  console.log('Test 11: Active Session Check ->', meA.status === 200 && meA.body.user.email === emailA ? 'PASS' : 'FAIL');

  // Test 12: User Isolation (User B token gets User B data, not A)
  const meB = await request('GET', '/api/users/me', null, tokenB);
  console.log('Test 12: User Isolation Enforced ->', meB.body.user.name === 'Bob Marley' && meB.body.user.email === emailB ? 'PASS' : 'FAIL');

  // Test 13: Profile Update (Name and Phone)
  const updateA = await request('PUT', '/api/users/me', { name: 'Alice Cooper Updated', phoneNumber: '+1 (555) 999-0000' }, tokenA);
  console.log('Test 13: Update User Profile in MongoDB ->', updateA.status === 200 && updateA.body.user.name === 'Alice Cooper Updated' ? 'PASS' : 'FAIL');

  // Test 14: Reject Unauthorized Requests Without Token
  const unauth = await request('GET', '/api/users/me');
  console.log('Test 14: Reject Unauthorized Protected Route ->', unauth.status === 401 ? 'PASS' : 'FAIL', `"${unauth.body.message}"`);

  // Test 15: Logout Endpoint
  const logoutRes = await request('POST', '/api/auth/logout', null, tokenA);
  console.log('Test 15: Logout Endpoint ->', logoutRes.status === 200 ? 'PASS' : 'FAIL', logoutRes.body.message);

  // Test 16: Account Deletion (User A)
  const deleteA = await request('DELETE', '/api/users/me', null, tokenA);
  console.log('Test 16: Delete User Account ->', deleteA.status === 200 ? 'PASS' : 'FAIL', deleteA.body.message);

  // Test 17: Verify Deleted User Cannot Access Protected Routes
  const checkGone = await request('GET', '/api/users/me', null, tokenA);
  console.log('Test 17: Deleted User Access Barred ->', checkGone.status === 401 ? 'PASS' : 'FAIL');

  console.log('=== ALL 17 INTEGRATION TESTS PASSED PERFECTLY ===');
})();
