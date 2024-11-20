import React from "react";

function Home() {
  return (
    <div>
      <h1>Welcome to the Home Page!</h1>
      <p>You have successfully logged in.</p>

      <div>
        <a href="/profile">
          <button type="button">Go to Profile</button>
        </a>
      </div>
      <div>
        <a href="/trade">
          <button type="button">Go to Trade Page</button>
        </a>
      </div>

      <form action="/logout" method="post">
        <button type="submit">Log Out</button>
      </form>
    </div>
  );
}

export default Home;
