import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
  vus: 50,
  duration: '10s',
};

export default function () {
  const res = http.get('http://127.0.0.1:8080/api/tokenbased');

  // THIS is the function that generates the 'checks' section!
  console.log("HEADERS RECEIVED:", JSON.stringify(res.headers));


  sleep(0.1);
}