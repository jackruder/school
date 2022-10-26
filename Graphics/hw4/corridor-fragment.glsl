#version 430

in vec2 fragmentST;
flat in int instance; //0:N 1:E 2:S 3:W

uniform int num_textures;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 m_matrix[4];
layout (binding=0) uniform sampler2D sampler0;
layout (binding=1) uniform sampler2D sampler1;
layout (binding=2) uniform sampler2D sampler2;
layout (binding=3) uniform sampler2D sampler3;

out vec4 color;

void main(void)
{	
  if (num_textures == 1) {
  	color=texture(sampler0, fragmentST);
  }
  else if (num_textures == 2) { // alternate textures
	if (instance == 0 || instance == 2) {
		color=texture(sampler0, fragmentST);
	} else {
		color=texture(sampler1, fragmentST);
	}
  }
  else if (num_textures == 3) { // 0th and 2nd corridors the same, 3 and 4 unique
	if (instance == 0 || instance == 2) {
		color=texture(sampler0, fragmentST);
	} else if (instance == 1) {
		color=texture(sampler1, fragmentST);
	}
	else { 
		color=texture(sampler2, fragmentST);
	}
	
  }
  else { // all different
	if (instance == 0) {
		color=texture(sampler0, fragmentST);
	}
	else if (instance == 1) {
		color=texture(sampler1, fragmentST);
	}
	else if (instance == 2) {
		color=texture(sampler2, fragmentST);
	}
	else {
		color=texture(sampler3, fragmentST);
	}
  }
}
