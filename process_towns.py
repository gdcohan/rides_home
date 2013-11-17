def read_file(source):
  temp = open(source)
  towns = temp.read().split('\n')[:-1]
  temp.close()

  return towns

def write_file(dest, data):
  out = open(dest, 'w')

  for q in data:
    out.write(q + '\n')

  out.close()

def main():
  town_list = read_file('student_towns.txt')

  unique_towns = []

  for q in town_list:
    if not q in unique_towns:
      unique_towns.append(q)

  unique_towns.sort()
  print(len(town_list),len(unique_towns))
  write_file('unique_towns.txt',unique_towns)

if __name__ == '__main__':
  main()
