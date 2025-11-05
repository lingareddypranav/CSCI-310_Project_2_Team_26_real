**Project Title:** BestLLM  
**Goal:** Build a complete Android application (Kotlin) with a Node.js \+ Express \+ PostgreSQL backend. The app allows USC students to share experiences and prompts about Large Language Models (LLMs), including posting, commenting, voting, and searching by tag. It must use MVVM \+ Repository \+ Room architecture for offline caching, Retrofit for networking, and SharedPreferences for token storage. Focus on local development; nothing should be hosted online.

The entire system has two main components:

---

### **Android Application**

The Android client uses Kotlin and follows the **MVVM architecture with a Repository layer and Room local database**.  
 It contains Activities, Fragments, ViewModels, Repositories, DAOs, Retrofit networking, and SharedPreferences.

**Architecture Overview:**

* UI Layer: Activities and Fragments handle interaction and rendering (LoginActivity, MainActivity, PostListFragment, PostDetailFragment, ProfileFragment).

* ViewModel Layer: PostViewModel, UserViewModel, and CommentViewModel use LiveData to observe data changes.

* Repository Layer: Handles communication between the ViewModels, Room, and Retrofit API. Repositories include PostRepository, UserRepository, CommentRepository, and AuthRepository.

* Data Layer: Contains Retrofit interfaces, Room database entities, and DAOs.

* Storage Layer: SharedPreferences manages user session tokens, and Room caches posts and comments for offline access.

**Android Configurations:**

* minSdk \= 26, targetSdk \= 34

* Base API URL \= `http://10.0.2.2:3000/` (local backend access for Android emulator)

* Dependencies: AppCompat, Material, Lifecycle ViewModel \+ LiveData, RecyclerView, Room (runtime \+ KTX \+ compiler), Retrofit, OkHttp, Gson.

**UI Flow:**

* LoginActivity: user authentication and registration.

* MainActivity: fragment host with navigation for posts, details, and profile.

* PostListFragment: displays cached or fetched posts.

* PostDetailFragment: shows a single post with comments and voting.

* ProfileFragment: shows current user info and logout option.

**Networking (Retrofit):**  
 Retrofit communicates with the backend using JSON. It includes an `ApiService` interface with endpoints:  
 `/auth/login`, `/auth/register`, `/users/:id`, `/posts`, `/posts/:id`, `/posts/:id/comments`, `/posts/:id/vote`.  
 RetrofitClient initializes Retrofit with the base URL and Gson converter.

**Repositories:**  
 Each repository mediates between the ViewModels, Room, and Retrofit.

* **PostRepository**: fetches posts (prefers Room cache, falls back to network), creates posts, handles votes, syncs with backend.

* **CommentRepository**: retrieves and adds comments, stores them locally.

* **UserRepository**: handles registration, login, and profile retrieval.

* **AuthRepository**: stores and retrieves auth tokens through SharedPreferences.

**Room Database:**  
 Defines `AppDatabase` with two entities: `PostEntity` and `CommentEntity`.  
 `PostDao` handles inserting and querying posts.  
 `CommentDao` handles comment retrieval and insertion.  
 Room ensures offline caching and enables MVVM data reactivity through LiveData updates when the repository syncs new data.

**SharedPreferences:**  
 Manages authentication tokens. Key: `"auth_token"`.  
 On login, the token is saved; on logout, it’s cleared. Stored using `Context.MODE_PRIVATE` (no encryption).

**ViewModels:**

* `PostViewModel`: exposes LiveData of post lists, wraps calls to PostRepository.

* `CommentViewModel`: exposes LiveData of comments.

* `UserViewModel`: manages user info and login/register actions.

**Execution Summary:**  
 When a user logs in, the app calls `/auth/login`, stores the token, and navigates to the post feed. The Repository checks Room for cached posts; if empty or stale, it fetches from the API, saves to Room, and notifies the ViewModel. Post creation, voting, and commenting trigger network requests followed by local cache updates. If offline, cached data remains available until reconnection.

### **Backend (Node.js \+ Express \+ PostgreSQL)**

The backend provides RESTful endpoints consumed by Retrofit. It’s entirely local, no cloud hosting.

**Server Structure:**

backend/  
  server.js  
  config/db.js  
  routes/  
    authRoutes.js  
    userRoutes.js  
    postRoutes.js  
    commentRoutes.js  
    voteRoutes.js  
  controllers/  
    authController.js  
    userController.js  
    postController.js  
    commentController.js  
    voteController.js  
  models/  
    userModel.js  
    postModel.js  
    commentModel.js  
    voteModel.js  
  middleware/authMiddleware.js

**Dependencies:** `express`, `pg`, `bcrypt`, `jsonwebtoken`, `cors`, `body-parser`, and `nodemon` for dev.  
 `db.js` connects to PostgreSQL using the `pg` library.  
 Each model defines SQL queries or uses parameterized statements to interact with tables.

**API Endpoints and Methods:**

* **Authentication:**

  * `POST /auth/register`: create a new user (hash password).

  * `POST /auth/login`: validate credentials, return JWT-like token (simple string OK).

* **Users:**

  * `GET /users/:id`: fetch user profile.

  * `PUT /users/:id`: update user info.

* **Posts:**

  * `GET /posts`: return all posts (optionally filtered by tag).

  * `POST /posts`: create post (requires auth).

  * `GET /posts/:id`: fetch post details.

  * `DELETE /posts/:id`: delete post if user is author.

* **Comments:**

  * `GET /posts/:id/comments`: list comments for a post.

  * `POST /posts/:id/comments`: add comment.

* **Votes:**

  * `POST /posts/:id/vote`: record upvote/downvote and update post vote count.

**Middleware:**  
 Simple JWT verification: decodes token, attaches user ID to request, and rejects unauthorized access.

**Database Schema (PostgreSQL):**

CREATE TABLE users (  
  id SERIAL PRIMARY KEY,  
  username VARCHAR(50) UNIQUE NOT NULL,  
  password\_hash TEXT NOT NULL,  
  email VARCHAR(100) UNIQUE NOT NULL  
);  
CREATE TABLE posts (  
  id SERIAL PRIMARY KEY,  
  author\_id INT REFERENCES users(id),  
  title TEXT NOT NULL,  
  content TEXT NOT NULL,  
  llm\_tag VARCHAR(50),  
  votes INT DEFAULT 0  
);  
CREATE TABLE comments (  
  id SERIAL PRIMARY KEY,  
  post\_id INT REFERENCES posts(id),  
  author\_id INT REFERENCES users(id),  
  text TEXT NOT NULL  
);  
CREATE TABLE votes (  
  id SERIAL PRIMARY KEY,  
  user\_id INT REFERENCES users(id),  
  post\_id INT REFERENCES posts(id),  
  type VARCHAR(10) CHECK (type IN ('up','down'))  
);  
CREATE TABLE tags (  
  id SERIAL PRIMARY KEY,  
  tag\_name VARCHAR(50)  
);

**Local Setup Commands:**

npm install  
node server.js

Server listens on port 3000\. Connect PostgreSQL with a local URI like `postgres://user:password@localhost:5432/bestllm`.

### **Integration Between Android and Backend**

The Android app communicates with this backend through Retrofit over HTTP.  
 All protected endpoints require the Authorization header (`Bearer <token>`).  
 Repositories inject the stored token from SharedPreferences into the Retrofit client.  
 Data flows:

* User logs in → token saved → used in header for all future requests.

* Posts and comments retrieved via GET calls; stored in Room.

* When online, Room is synced automatically with latest backend data.

* When offline, UI reads directly from Room database.

### **Build Instructions Summary**

1. Start PostgreSQL and create database `bestllm`.

2. Run backend with `node server.js`.

3. Open Android Studio → sync Gradle → run app on emulator.

4. The app will connect to backend at `http://10.0.2.2:3000/`.

### **Expected Behavior**

* First launch → Login screen (register or login).

* Successful login → Main screen showing posts list.

* Selecting a post → Detail view with comments and vote buttons.

* Profile tab → shows username and logout button.

* Offline mode → cached posts and comments still visible from Room.

This description fully defines every component of **BestLLM**, including technologies, architecture, dependencies, data flow, backend schema, API endpoints, Android class structure, and local configurations.  
 The IDE should generate:

1. An Android app in Kotlin using MVVM \+ Repository \+ Room \+ Retrofit \+ SharedPreferences.

2. A Node.js \+ Express backend with PostgreSQL using REST APIs and basic JWT auth.  
    Everything runs locally, connected via `http://10.0.2.2:3000/`.  
    No cloud deployment, encryption, or external services required.  
    This is the complete implementation-level specification for automated code generation of the **BestLLM** mobile app project.

