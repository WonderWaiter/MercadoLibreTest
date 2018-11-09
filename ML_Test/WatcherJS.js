var nodemailer = require('nodemailer');
const https = require('http');
const readline = require('readline');

var lastState = 'OK';
var tim;
var yourEmail;

function pingServer(){
	
	https.get('http://localhost:8889/ping', (resp) => {
	  let data = '';
	  resp.on('data', (chunk) => {
		data += chunk;
	  });
	  resp.on('end', () => {
	    lastState = 'OK';
		console.log(data);
	  });
	}).on("error", (err) => {	  
	  console.log("Error: " + err.message);
	  if(lastState == 'OK'){
		lastState = 'ERROR';
		
		nodemailer.createTestAccount((er, account) => {
		    let transporter = nodemailer.createTransport({
			host: 'smtp.ethereal.email',
			port: 587,
			secure: false,
			auth: {
			  user: account.user,
			  pass: account.pass
			}
		  });
		  let mailOptions = {
			from: '"Una cuenta de mail" <mimail@example1232.com>',
			to: yourEmail,
			subject: 'Server is Down!!!',
			text: 'Error connecting to webserver.\r\n' + err.message
		  };
		  transporter.sendMail(mailOptions, (error, info) => {
			if (error) {
				return console.log(error);
			}
			console.log('Message sent: %s', info.messageId);
			console.log('Preview URL: %s', nodemailer.getTestMessageUrl(info));
		  });
		});		
	  }
	  lastState = 'ERROR';
	});
}

const r1 = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});
r1.question('Enter your email: \r\n', (subInput) => {
    yourEmail = subInput; 
    tim = setInterval(pingServer, 2000);
	r1.question('Submit to stop \r\n', (subIn) => {
	  clearInterval(tim);
	  console.log('Watcher stopped.');
	  r1.close();
	});
});